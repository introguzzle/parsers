package ru.introguzzle.parser.json.entity;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parser.common.convert.ConverterFactory;
import ru.introguzzle.parser.common.convert.Converter;
import ru.introguzzle.parser.common.UntypedMap;
import ru.introguzzle.parser.common.visit.Visitable;
import ru.introguzzle.parser.common.visit.Visitor;
import ru.introguzzle.parser.json.mapping.JSONObjectConvertable;
import ru.introguzzle.parser.json.mapping.reference.StandardCircularReferenceStrategies.CircularReference;
import ru.introguzzle.parser.xml.entity.XMLDocument;
import ru.introguzzle.parser.xml.entity.XMLDocumentConvertable;

import java.io.Serial;
import java.util.*;

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

    @Serial
    private static final long serialVersionUID = -697931640108868641L;

    private final Map<Object, Object> referenceMap = new IdentityHashMap<>();

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
                ? (JSONObject) referenceMap.get(reference.getValue())
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
    public JSONObject toJSONObject() {
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

    @Override
    public XMLDocument toXMLDocumentWithMetadata() {
        return CONVERTER.convertWithMetadata(this);
    }
}
