package ru.introguzzle.parsers.json.entity;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.mapping.MappingException;
import ru.introguzzle.parsers.common.util.UntypedMap;
import ru.introguzzle.parsers.common.convert.ConverterFactory;
import ru.introguzzle.parsers.common.convert.Converter;
import ru.introguzzle.parsers.common.visit.Visitable;
import ru.introguzzle.parsers.common.visit.Visitor;
import ru.introguzzle.parsers.json.mapping.JSONObjectConvertable;
import ru.introguzzle.parsers.json.mapping.deserialization.ObjectMapper;
import ru.introguzzle.parsers.json.mapping.reference.StandardCircularReferenceStrategies.CircularReference;
import ru.introguzzle.parsers.json.mapping.serialization.JSONMapper;
import ru.introguzzle.parsers.xml.entity.XMLDocument;
import ru.introguzzle.parsers.xml.entity.XMLDocumentConvertable;

import java.io.Serial;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a JSON object as a map of key-value pairs.
 * <p>
 * It implements both {@link JSONStringConvertable} and {@link JSONObjectConvertable} interfaces.
 * </p>
 */
@Getter
public class JSONObject extends UntypedMap implements
        JSONStringConvertable, JSONObjectConvertable,
        XMLDocumentConvertable, Visitable<JSONObject, Visitor<JSONObject>> {
    public static final @NotNull ConverterFactory FACTORY;
    public static final Converter<JSONObject, XMLDocument> CONVERTER;

    static {
        FACTORY = ConverterFactory.getFactory();
        CONVERTER = FACTORY.getJSONDocumentToXMLConverter();
    }

    private static final Map<Class<?>, ObjectMapper> MAPPERS = new HashMap<>();
    private transient WeakReference<JSONMapper> applier;

    @Serial
    private static final long serialVersionUID = -697931640108868641L;

    public JSONObject() {
        super();
    }

    public JSONObject(Map<? extends String, ?> map) {
        super(map);
    }

    public String getString(String key) {
        return get(key, String.class);
    }

    public Number getNumber(String key) {
        return get(key, Number.class);
    }

    public Boolean getBoolean(String key) {
        return get(key, Boolean.class);
    }

    public JSONObject getObject(String key) {
        Object value = get(key);
        return value instanceof CircularReference<?> reference
                ? Objects.requireNonNull(applier.get()).toJSONObject(reference.getValue())
                : (JSONObject) value;
    }

    public JSONArray getArray(String key) {
        return get(key, JSONArray.class);
    }

    @Override
    public Iterator<?> getIterator() {
        return entrySet().iterator();
    }

    @Override
    public String getOpeningSymbol() {
        return "{";
    }

    @Override
    public String getClosingSymbol() {
        return "}";
    }

    @Override
    public @NotNull JSONObject toJSONObject() {
        return this;
    }

    @Override
    public XMLDocument toXMLDocument() {
        return CONVERTER.convert(this);
    }

    public JSONObject flatten() {
        return flatten(FACTORY.getTextPlaceholder());
    }

    public JSONObject flatten(String textKey) {
        Object flattened = flatten(this, textKey);
        if (flattened instanceof JSONObject) {
            return (JSONObject) flattened;
        }

        throw new ClassCastException("Flattened root is not a JSONObject");
    }

    private static Object flatten(JSONObject object, String textKey) {
        if (object.size() == 1 && object.containsKey(textKey)) {
            return object.get(textKey);
        }

        JSONObject result = new JSONObject();

        for (Map.Entry<String, Object> entry : object.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof JSONObject o) {
                result.put(key, flatten(o, textKey));
            } else if (value instanceof JSONArray a) {
                result.put(key, flatten(a, textKey));
            } else {
                result.put(key, value);
            }
        }

        return result;
    }

    private static Object flatten(JSONArray array, String textKey) {
        JSONArray result = new JSONArray();

        for (Object item : array) {
            if (item instanceof JSONObject o) {
                result.add(flatten(o, textKey));
            } else if (item instanceof JSONArray a) {
                result.add(flatten(a, textKey));
            } else {
                result.add(item);
            }
        }

        return result;
    }

    public static void bindTo(Class<?> type, ObjectMapper mapper) {
        MAPPERS.put(type, mapper);
    }

    public static void unbind(Class<?> type) {
        MAPPERS.remove(type);
    }

    public static void unbindAll() {
        MAPPERS.clear();
    }

    public <T> T toObject(Class<T> type) {
        ObjectMapper associate = MAPPERS.get(type);
        if (associate == null) {
            throw new MappingException("No mapper present for " + type);
        }

        return associate.toObject(this, type);
    }

    public void setApplier(JSONMapper mapper) {
        if (applier == null) {
            applier = new WeakReference<>(mapper);
        }
    }

    @Override
    public XMLDocument toXMLDocumentWithMetadata() {
        return CONVERTER.convertWithMetadata(this);
    }
}
