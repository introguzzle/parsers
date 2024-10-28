package ru.introguzzle.parser.json.entity;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parser.common.convert.ConverterFactory;
import ru.introguzzle.parser.common.convert.Converter;
import ru.introguzzle.parser.common.UntypedMap;
import ru.introguzzle.parser.json.mapping.JSONObjectConvertable;
import ru.introguzzle.parser.json.mapping.context.StandardCircularReferenceStrategies;
import ru.introguzzle.parser.common.utilities.NamingUtilities;
import ru.introguzzle.parser.common.utilities.ReflectionUtilities;
import ru.introguzzle.parser.json.visitor.JSONObjectVisitor;
import ru.introguzzle.parser.xml.XMLDocument;
import ru.introguzzle.parser.xml.XMLDocumentConvertable;

import java.io.Serial;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

/**
 * Represents a JSON object as a map of key-value pairs.
 * <p>
 * This class extends {@link LinkedHashMap} to provide a natural-order sorted structure for JSON objects,
 * enabling easy serialization to and from JSON strings. It implements both
 * {@link JSONStringConvertable} and {@link JSONObjectConvertable} interfaces.
 * </p>
 */
public class JSONObject extends UntypedMap implements
        JSONStringConvertable, JSONObjectConvertable,
        XMLDocumentConvertable, Consumer<JSONObjectVisitor> {

    public static final @NotNull ConverterFactory FACTORY = ConverterFactory.getFactory();
    public static final Converter<JSONObject, XMLDocument> CONVERTER = FACTORY.getJSONDocumentToXMLConverter();

    @Serial
    private static final long serialVersionUID = -697931640108868641L;

    /** A map to track references for circular references. */
    private final Map<Object, Object> referenceMap = new HashMap<>();

    public JSONObject() {
        super();
    }

    public JSONObject(Map<? extends String, ?> map) {
        super(map);
    }

    /**
     * Maps this JSONObject to an instance of a specified type.
     *
     * @param type the class type to map to
     * @param <T>  the type to be returned
     * @return an instance of the specified type populated with values from this JSONObject
     */
    @SuppressWarnings("unchecked")
    public <T> @NotNull T map(Class<? extends T> type) {
        try {
            if (Map.class.isAssignableFrom(type)) {
                if (type == Map.class) {
                    return (T) new HashMap<>(this);
                }

                Map<String, Object> map = (Map<String, Object>) type.getConstructor().newInstance();
                map.putAll(this);

                return (T) map;
            }

            T instance = type.getConstructor().newInstance();
            referenceMap.put(this, instance);

            List<Field> fields = ReflectionUtilities.getAllFields(type);
            for (Field field : fields) {
                field.setAccessible(true);
                String snakeCaseName = NamingUtilities.toSnakeCase(field.getName());
                Object value = match(field.getType(), get(snakeCaseName));
                if (StandardCircularReferenceStrategies.PLACEHOLDER.equals(value)) {
                    value = referenceMap.get(this);
                }

                field.set(instance, value);
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Matches a given value to the expected type.
     *
     * @param type  the expected type
     * @param value the value to match
     * @return the matched value
     * @throws InvocationTargetException if an exception occurs during reflection
     * @throws NoSuchMethodException      if a required method is not found
     * @throws IllegalAccessException     if access to a method is denied
     */
    private Object match(Class<?> type, Object value)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        if (value instanceof JSONObject object) {
            if (referenceMap.containsKey(object)) {
                return referenceMap.get(object);
            }

            return object.map(type);
        }

        if (value instanceof JSONArray array) {
            return handleArray(type, array);
        }

        return handleSimpleType(type, value);
    }

    /**
     * Handles the mapping of a JSONArray to the appropriate Java type.
     *
     * @param fieldType the expected field type
     * @param array     the JSONArray to process
     * @return the mapped Java object (array, set, or the original array)
     * @throws InvocationTargetException if an exception occurs during reflection
     * @throws NoSuchMethodException      if a required method is not found
     * @throws IllegalAccessException     if access to a method is denied
     */
    private Object handleArray(Class<?> fieldType, JSONArray array)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        if (fieldType.isArray()) {
            int size = array.size();
            Class<?> componentType = fieldType.getComponentType();
            Object resultArray = Array.newInstance(componentType, size);
            for (int i = 0; i < size; i++) {
                Object element = match(componentType, array.get(i));
                Array.set(resultArray, i, element);
            }

            return resultArray;
        }

        if (Set.class.isAssignableFrom(fieldType)) {
            Set<Object> resultSet = new HashSet<>();
            for (Object element : array) {
                element = match(fieldType, element);
                resultSet.add(element);
            }

            return resultSet;
        }

        return array;
    }

    /** A map linking sets of number types to their respective methods for value retrieval. */
    private static final Map<Set<Class<?>>, String> NUMBER_METHOD_MAP = Map.of(
            Set.of(int.class, Integer.class), "intValue",
            Set.of(double.class, Double.class), "doubleValue",
            Set.of(float.class, Float.class), "floatValue",
            Set.of(byte.class, Byte.class), "byteValue",
            Set.of(short.class, Short.class), "shortValue",
            Set.of(long.class, Long.class), "longValue"
    );

    /**
     * Converts a primitive number type to its corresponding boxed type.
     *
     * @param primitiveNumberType the primitive number type
     * @return the corresponding boxed type
     */
    private Class<? extends Number> box(Class<?> primitiveNumberType) {
        return switch (primitiveNumberType.getSimpleName()) {
            case "byte"    -> Byte.class;
            case "short"   -> Short.class;
            case "int"     -> Integer.class;
            case "long"    -> Long.class;
            case "float"   -> Float.class;
            case "double"  -> Double.class;
            default -> throw new IllegalStateException("Unexpected value: " + primitiveNumberType.getSimpleName());
        };
    }

    /**
     * Handles simple type mapping for the value.
     *
     * @param fieldType the expected field type
     * @param value     the value to handle
     * @return the mapped value
     * @throws NoSuchMethodException      if a required method is not found
     * @throws InvocationTargetException   if an exception occurs during reflection
     * @throws IllegalAccessException      if access to a method is denied
     */
    @SuppressWarnings("unchecked")
    private Object handleSimpleType(Class<?> fieldType, Object value)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (fieldType.isPrimitive() && value == null) {
            // Field is primitive and null is value which is illegal,
            // so we need to get primitive default value
            return getPrimitiveDefaultValue(fieldType);
        }

        if (fieldType == String.class
                || fieldType == Character.class
                || fieldType == char.class
                || fieldType == Boolean.class
                || fieldType == boolean.class) {
            return value;
        }

        Class<? extends Number> numberClass = fieldType.isPrimitive()
                ? box(fieldType)
                : (Class<? extends Number>) fieldType;

        if (Number.class.isAssignableFrom(numberClass)) {
            if (value == null) {
                return null;
            }

            Number number = (Number) value;
            for (var entry : NUMBER_METHOD_MAP.entrySet()) {
                for (Class<?> c : entry.getKey()) {
                    if (c == fieldType) {
                        Method method = number.getClass().getMethod(entry.getValue());
                        method.setAccessible(true);

                        return method.invoke(number);
                    }
                }
            }
        }

        return value;
    }

    /**
     * Gets the default value for a given primitive type.
     *
     * @param fieldType the primitive type
     * @return the default value for the primitive type
     */
    private static @NotNull Object getPrimitiveDefaultValue(Class<?> fieldType) {
        return switch (fieldType.getSimpleName()) {
            case "boolean" -> false;
            case "byte"    -> (byte) 0;
            case "short"   -> (short) 0;
            case "int"     -> 0;
            case "long"    -> 0L;
            case "float"   -> 0.0f;
            case "double"  -> 0.0;
            case "char"    -> '\u0000';
            default        -> throw new IllegalStateException("Unexpected value: " + fieldType.getSimpleName());
        };
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

    @Override
    public void accept(JSONObjectVisitor visitor) {
        visitor.visit(this);
    }
}
