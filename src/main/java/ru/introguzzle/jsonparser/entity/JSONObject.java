package ru.introguzzle.jsonparser.entity;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.jsonparser.ReflectionUtilities;
import ru.introguzzle.jsonparser.mapping.JSONObjectConvertable;
import ru.introguzzle.jsonparser.mapping.NamingUtilities;
import ru.introguzzle.jsonparser.mapping.context.CircularReferenceStrategy;

import java.io.Serial;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Supplier;

public class JSONObject extends LinkedHashMap<String, Object>
        implements JSONStringConvertable, JSONObjectConvertable {
    @Serial
    private static final long serialVersionUID = -697931640108868641L;
    private final Map<Object, Object> referenceMap = new HashMap<>();

    public <M extends Map<Object, Object>> M map(Supplier<M> supplier) {
        M map = supplier.get();
        map.putAll(this);
        return map;
    }

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

    private Object handleArray(Class<?> fieldType,
                               JSONArray array)
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

    private static final Map<Set<Class<?>>, String> NUMBER_METHOD_MAP = Map.of(
            Set.of(int.class, Integer.class), "intValue",
            Set.of(double.class, Double.class), "doubleValue",
            Set.of(float.class, Float.class), "floatValue",
            Set.of(byte.class, Byte.class), "byteValue",
            Set.of(short.class, Short.class), "shortValue",
            Set.of(long.class, Long.class), "longValue"
    );

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

    @SuppressWarnings("unchecked")
    private Object handleSimpleType(Class<?> fieldType, Object value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (fieldType.isPrimitive() && value == null) {
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

    public <T> T get(String key, Class<? extends T> type) {
        return type.cast(get(key));
    }

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
