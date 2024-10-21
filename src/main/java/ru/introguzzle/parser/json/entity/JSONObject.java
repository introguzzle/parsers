package ru.introguzzle.parser.json.entity;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parser.json.mapping.JSONObjectConvertable;
import ru.introguzzle.parser.json.mapping.context.CircularReferenceStrategy;
import ru.introguzzle.parser.json.utilities.NamingUtilities;
import ru.introguzzle.parser.json.utilities.ReflectionUtilities;

import java.io.Serial;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Supplier;

/**
 * Represents a JSON object as a map of key-value pairs.
 * <p>
 * This class extends {@link LinkedHashMap} to provide a natural-order sorted structure for JSON objects,
 * enabling easy serialization to and from JSON strings. It implements both
 * {@link JSONStringConvertable} and {@link JSONObjectConvertable} interfaces.
 * </p>
 */
public class JSONObject extends LinkedHashMap<String, Object>
        implements JSONStringConvertable, JSONObjectConvertable {

    @Serial
    private static final long serialVersionUID = -697931640108868641L;

    /** A map to track references for circular references. */
    private final Map<Object, Object> referenceMap = new HashMap<>();

    /**
     * Maps this JSONObject to a new instance of a specified Map type.
     *
     * @param supplier a supplier function to create a new map instance
     * @param <M>     the type of the map
     * @return a map containing the entries of this JSONObject
     */
    public <M extends Map<Object, Object>> M map(Supplier<M> supplier) {
        M map = supplier.get();
        map.putAll(this);
        return map;
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
                if (Map.class == type) {
                    return (T) new HashMap<>(this);
                }
                return type.getConstructor(Map.class).newInstance(this);
            }

            T instance = type.getConstructor().newInstance();
            referenceMap.put(this, instance);

            List<Field> fields = ReflectionUtilities.getAllFields(type);
            for (Field field : fields) {
                field.setAccessible(true);
                String snakeCaseName = NamingUtilities.toSnakeCase(field.getName());
                Object value = match(field.getType(), get(snakeCaseName));
                if (CircularReferenceStrategy.PLACEHOLDER.equals(value)) {
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
            Class<?> componentType = fieldType.getComponentType();
            Object resultArray = Array.newInstance(componentType, array.size());
            for (int i = 0; i < array.size(); i++) {
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
                        return number.getClass().getMethod(entry.getValue()).invoke(number);
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

    /**
     * Retrieves the value associated with the specified key, casting it to the desired type.
     *
     * @param key  the key for the desired value
     * @param type the class type to cast to
     * @param <T>  the type to be returned
     * @return the value associated with the key cast to the specified type
     */
    public <T> T get(String key, Class<? extends T> type) {
        return type.cast(get(key));
    }

    /**
     * Retrieves the value associated with the specified key, casting it to the desired type,
     * or returns a default value if the key does not exist.
     *
     * @param key         the key for the desired value
     * @param type        the class type to cast to
     * @param defaultValue the default value to return if the key does not exist
     * @param <T>         the type to be returned
     * @return the value associated with the key cast to the specified type, or the default value
     */
    public <T> T get(String key, Class<? extends T> type, T defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }

        return type.cast(value);
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
}
