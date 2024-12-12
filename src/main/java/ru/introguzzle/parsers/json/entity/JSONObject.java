package ru.introguzzle.parsers.json.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Represents a JSON object as a map of key-value pairs.
 * <p>
 * This class extends {@link UntypedMap} and provides methods to access and manipulate JSON data.
 * It implements several interfaces to facilitate conversion to JSON strings, XML documents, and visitation patterns.
 * </p>
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Store and retrieve JSON key-value pairs.</li>
 *   <li>Access values as specific types (String, Number, Boolean, {@code JSONObject}, {@code JSONArray}).</li>
 *   <li>Convert to JSON string representation.</li>
 *   <li>Convert to XML document representation.</li>
 *   <li>Flatten nested JSON structures.</li>
 *   <li>Bind custom object mappers for specific types.</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * JSONObject jsonObject = new JSONObject();
 * jsonObject.put("name", "Alice");
 * jsonObject.put("age", 30);
 * jsonObject.put("isStudent", false);
 *
 * String name = jsonObject.getString("name"); // "Alice"
 * int age = jsonObject.getNumber("age").intValue(); // 30
 * boolean isStudent = jsonObject.getBoolean("isStudent"); // false
 *
 * String jsonString = jsonObject.toJSONString();
 * System.out.println(jsonString); // {"name":"Alice","age":30,"isStudent":false}
 * }</pre>
 *
 * <h5>
 * Note: all methods that are related to storing elements may throw {@link IllegalArgumentException}
 * if class of element to be stored is not permitted according to {@linkplain JSONObject#PERMITTED_CLASSES}.
 * This behaviour depends on boolean entityValidationEnabled flag in configuration
 * </h5>
 *
 * @see UntypedMap
 * @see JSONStringConvertable
 * @see JSONObjectConvertable
 * @see XMLDocumentConvertable
 * @see Visitable
 */
public class JSONObject extends UntypedMap implements
        JSONStringConvertable, JSONObjectConvertable,
        XMLDocumentConvertable, Visitable<JSONObject, Visitor<JSONObject>>, Serializable {

    /**
     * The {@link ConverterFactory} instance used for obtaining converters.
     */
    public static final @NotNull ConverterFactory FACTORY;

    /**
     * The converter used to transform a {@code JSONObject} into an {@link XMLDocument}.
     */
    public static final Converter<JSONObject, XMLDocument> CONVERTER;

    /**
     * A set of classes that are permitted to be stored as values in the {@code JSONObject}.
     * This includes {@code Number}, {@code String}, {@code JSONObject}, {@code JSONArray},
     * {@code Boolean}, and {@code CircularReference}.
     */
    public static final Set<Class<?>> PERMITTED_CLASSES;
    private static final DeepCopier COPIER = new DeepCopier();

    static {
        FACTORY = ConverterFactory.getFactory();
        CONVERTER = FACTORY.getJSONDocumentToXMLConverter();
        PERMITTED_CLASSES = Types.PERMITTED_CLASSES;
    }

    /**
     * A map of custom {@link ObjectMapper} instances associated with specific types.
     */
    private static final Map<Type, ObjectMapper> MAPPERS = new HashMap<>();

    /**
     * The {@link JSONMapper} that produced this object.
     */
    private volatile transient JSONMapper producer;

    @Serial
    private static final long serialVersionUID = -697931640108868641L;

    /**
     * Constructs an empty {@code JSONObject} with {@link LinkedHashMap}
     * as map to delegate
     */
    public JSONObject() {
        super();
    }

    /**
     * Constructs a {@code JSONObject} initialized with the specified map to delegate
     *
     * @param map the map to delegate
     * @throws IllegalArgumentException if class of any value is not permitted or any key is {@code null}
     */
    public JSONObject(Map<? extends String, ?> map) {
        super(Types.requirePermittedTypes(map));
    }

    /**
     * Retrieves the value associated with the specified key as a {@code String}.
     *
     * @param key the key whose associated value is to be returned
     * @return the value associated with the key as a {@code String}, or {@code null} if not found
     * @throws ClassCastException if the value is not a {@code String}
     */
    public String getString(String key) {
        return get(key, String.class);
    }

    /**
     * Retrieves the value associated with the specified key as a {@code Number}.
     *
     * @param key the key whose associated value is to be returned
     * @return the value associated with the key as a {@code Number}, or {@code null} if not found
     * @throws ClassCastException if the value is not a {@code Number}
     */
    public Number getNumber(String key) {
        return get(key, Number.class);
    }

    /**
     * Retrieves the value associated with the specified key as a {@code Boolean}.
     *
     * @param key the key whose associated value is to be returned
     * @return the value associated with the key as a {@code Boolean}, or {@code null} if not found
     * @throws ClassCastException if the value is not a {@code Boolean}
     */
    public Boolean getBoolean(String key) {
        return get(key, Boolean.class);
    }

    /**
     * Retrieves the value associated with the specified key as a {@code JSONObject}.
     * If the value is a {@code CircularReference}, it dereferences it using the producer's {@code toJSONObject} method.
     *
     * @param key the key whose associated value is to be returned
     * @return the value associated with the key as a {@code JSONObject}, or {@code null} if not found
     * @throws ClassCastException if the value is not a {@code JSONObject} or a {@code CircularReference}
     */
    public JSONObject getObject(String key) {
        Object value = get(key);
        return value instanceof CircularReference<?> reference
                ? Objects.requireNonNull(producer.toJSONObject(reference.dereference()))
                : (JSONObject) value;
    }

    /**
     * Retrieves element by key in dot notation
     * @param key key in dot notation
     * @param type class of element of last key
     * @return element of last key
     * @param <T> type of element of last key
     */
    public <T> T getTraverse(String key, Class<? extends T> type) {
        String[] keys = key.split("\\.");
        if (keys.length == 1) {
            return get(key, type);
        }

        JSONObject last = getObject(keys[0]);
        for (int i = 1; i < keys.length - 1; i++) {
            last = last.getObject(keys[i]);
        }

        return last.get(keys[keys.length - 1], type);
    }

    /**
     * Associates the specified value with the specified key in dot notation in this object.
     * Only values of permitted types are allowed.
     *
     * @param key   the key in dot notation with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @return the previous value associated with the key, or {@code null} if there was no mapping
     * @throws IllegalArgumentException if the value's class is not permitted or key is {@code null}
     */
    public Object putTraverse(String key, Object value) {
        String[] keys = key.split("\\.");
        if (keys.length == 1) {
            return put(key, value);
        }

        JSONObject last = getObject(keys[0]);
        for (int i = 1; i < keys.length - 1; i++) {
            last = last.getObject(keys[i]);
        }

        return last.put(keys[keys.length - 1], value);
    }

    /**
     * Associates the specified value with the specified key in this object.
     * Only values of permitted types are allowed.
     *
     * @param key   the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @return the previous value associated with the key, or {@code null} if there was no mapping
     * @throws IllegalArgumentException if the value's class is not permitted or key is {@code null}
     */
    @Override
    public @Nullable Object put(@NotNull String key, Object value) {
        return putChecked(key, value);
    }

    /**
     * Associates the specified value with the specified key in this object after checking its type.
     * Only values of permitted types are allowed.
     *
     * @param key   the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @return the previous value associated with the key, or {@code null} if there was no mapping
     * @throws IllegalArgumentException if the value's class is not permitted or key is {@code null}
     */
    public Object putChecked(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Null key is not permitted in JSONObject");
        }

        return super.put(key, Types.requirePermittedType(value, EntityUnion.OBJECT));
    }

    /**
     * {@inheritDoc}
     * @throws IllegalArgumentException if class of any value is not permitted or any key is {@code null}
     */
    @Override
    public void putAll(@NotNull Map<? extends String, ?> m) {
        super.putAll(Types.requirePermittedTypes(m));
    }

    /**
     * Retrieves the value associated with the specified key as a {@code JSONArray}.
     *
     * @param key the key whose associated value is to be returned
     * @return the value associated with the key as a {@code JSONArray}, or {@code null} if not found
     * @throws ClassCastException if the value is not a {@code JSONArray}
     */
    public JSONArray getArray(String key) {
        return get(key, JSONArray.class);
    }

    /**
     * Returns an iterator over the entries in this object.
     *
     * @return an iterator over the entries in this object
     */
    @Override
    public Iterator<?> getIterator() {
        return entrySet().iterator();
    }

    /**
     * Returns the opening symbol for this JSON structure, which is "{".
     *
     * @return the opening symbol "{"
     */
    @Override
    public String getOpeningSymbol() {
        return "{";
    }

    /**
     * Returns the closing symbol for this JSON structure, which is "}".
     *
     * @return the closing symbol "}"
     */
    @Override
    public String getClosingSymbol() {
        return "}";
    }

    /**
     * Returns this {@code JSONObject}.
     *
     * @return this {@code JSONObject}
     */
    @Override
    public @NotNull JSONObject toJSONObject() {
        return this;
    }

    /**
     * Converts this {@code JSONObject} into an {@link XMLDocument}.
     *
     * @return an {@code XMLDocument} representation of this object
     */
    @Override
    public XMLDocument toXMLDocument() {
        return CONVERTER.convert(this);
    }

    /**
     * Flattens this {@code JSONObject} that was transformed from {@link XMLDocument}, simplifying nested structures.
     * <br>
     * Uses the default text placeholder from the converter factory.
     *
     * @return a flattened {@code JSONObject}
     */
    public JSONObject flatten() {
        return flatten(FACTORY.getTextPlaceholder());
    }

    /**
     * Flattens this {@code JSONObject} that was transformed from {@link XMLDocument}, simplifying nested structures.
     *
     * @param textPlaceholder the placeholder key used for text content in {@link XMLDocument}
     * @return a flattened {@code JSONObject}
     */
    public JSONObject flatten(String textPlaceholder) {
        Object flattened = flatten(this, textPlaceholder);
        if (flattened instanceof JSONObject) {
            return (JSONObject) flattened;
        }

        throw new ClassCastException("Flattened root is not a JSONObject");
    }

    /**
     * Recursively flattens a {@code JSONObject} that was transformed from {@link XMLDocument},
     * simplifying nested structures.
     *
     * @param object          the {@code JSONObject} that was transformed from {@link XMLDocument} to flatten
     * @param textPlaceholder the placeholder key used for text content in {@link XMLDocument}
     * @return the flattened object
     */
    private static Object flatten(JSONObject object, String textPlaceholder) {
        if (object.size() == 1 && object.containsKey(textPlaceholder)) {
            return object.get(textPlaceholder);
        }

        JSONObject result = new JSONObject();

        for (Map.Entry<String, Object> entry : object.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof JSONObject o) {
                result.put(key, flatten(o, textPlaceholder));
            } else if (value instanceof JSONArray a) {
                result.put(key, flatten(a, textPlaceholder));
            } else {
                result.put(key, value);
            }
        }

        return result;
    }

    /**
     * Recursively flattens a {@code JSONArray} that was transformed from {@link XMLDocument},
     * simplifying nested structures.
     *
     * @param array           the {@code JSONArray} that was transformed from {@link XMLDocument} to flatten
     * @param textPlaceholder the placeholder key used for text content in {@link XMLDocument}
     * @return the flattened array
     */
    private static Object flatten(JSONArray array, String textPlaceholder) {
        JSONArray result = new JSONArray();

        for (Object item : array) {
            if (item instanceof JSONObject o) {
                result.add(flatten(o, textPlaceholder));
            } else if (item instanceof JSONArray a) {
                result.add(flatten(a, textPlaceholder));
            } else {
                result.add(item);
            }
        }

        return result;
    }

    /**
     * Binds a custom {@link ObjectMapper} to the specified type.
     * This allows for custom deserialization of JSON objects into specific types.
     *
     * @param type   the target class to bind the mapper to
     * @param mapper the {@code ObjectMapper} to use for deserialization
     */
    static void bindTo(Class<?> type, ObjectMapper mapper) {
        MAPPERS.put(type, mapper);
    }

    /**
     * Unbinds the {@link ObjectMapper} associated with the specified type.
     *
     * @param type the target class whose mapper is to be unbound
     */
    static void unbind(Class<?> type) {
        MAPPERS.remove(type);
    }

    /**
     * Unbinds all {@link ObjectMapper} instances from their associated types.
     */
    static void unbindAll() {
        MAPPERS.clear();
    }

    /**
     * Deserializes this {@code JSONObject} into an object of the specified type using a bound {@link ObjectMapper}.
     *
     * @param type the target type to deserialize into
     * @return an instance of the specified type
     * @throws MappingException if no mapper is bound to the specified type
     * @see ObjectMapper
     */
    public Object toObject(Type type) {
        ObjectMapper associate = MAPPERS.get(type);
        if (associate == null) {
            throw new MappingException("No mapper present for " + type);
        }

        return associate.toObject(this, type);
    }

    /**
     * Sets the {@link JSONMapper} that produced this object.
     * This method can only be called once.
     *
     * @param mapper the {@code JSONMapper} that produced this object
     */
    void setProducer(JSONMapper mapper) {
        // Can be set only once
        synchronized (this) {
            if (producer == null) {
                producer = mapper;
            }
        }
    }

    /**
     * Converts this {@code JSONObject} into an {@link XMLDocument}, including metadata.
     *
     * @return an {@code XMLDocument} representation of this object with metadata
     */
    @Override
    public XMLDocument toXMLDocumentWithMetadata() {
        return CONVERTER.convertWithMetadata(this);
    }

    /**
     * Class of delegate map of this object
     * @return class of delegate map of this object
     */
    @Override
    public Class<? extends Map<String, Object>> getImplementationClass() {
        return super.getImplementationClass();
    }

    /**
     *
     * Creates a new immutable view {@code JSONObject} of this object with map that doesn't
     * support adding new entries as delegate
     *
     * @return immutable view {@code JSONObject} of this object with immutable map as delegate
     * <br>
     * For reference: {@linkplain java.util.Collections#unmodifiableMap(Map)}
     */
    public JSONObject asImmutable() {
        return new JSONObject(Collections.unmodifiableMap(this));
    }

    public JSONObject deepCopy() {
        return COPIER.createDeepCopy(this);
    }

    @Override
    public String toString() {
        return toJSONString();
    }
}
