package ru.introguzzle.parser.json.mapping;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parser.json.entity.JSONArray;
import ru.introguzzle.parser.json.mapping.context.MappingContext;
import ru.introguzzle.parser.json.mapping.type.JSONType;
import ru.introguzzle.parser.common.utilities.ReflectionUtilities;
import ru.introguzzle.parser.json.entity.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectToJSONMapperImpl implements ObjectToJSONMapper {
    private static final Set<Class<?>> EXCLUDED = Set.of(
        String.class, Boolean.class, Number.class, Character.class, Date.class
    );

    private static final Map<Class<?>, List<Field>> CLASS_FIELDS_CACHE =
            new ConcurrentHashMap<>();

    private static synchronized List<Field> getCachedFields(Class<?> type) {
        return CLASS_FIELDS_CACHE.computeIfAbsent(type, ReflectionUtilities::getAllFields);
    }

    private static boolean isExcluded(Class<?> type) {
        if (type.isPrimitive()) return true;

        for (Class<?> excluded : EXCLUDED)
            if (type == excluded || excluded.isAssignableFrom(type))
                return true;

        return false;
    }

    @Override
    public JSONObject toJSONObject(@NotNull Object object, MappingContext context) {
        return (JSONObject) toJSONObjectRecursive(object, context);
    }

    @Override
    public Object toJSONObjectRecursive(@Nullable Object object, MappingContext context) {
        if (object == null) return null;
        if (context.getReferences().containsKey(object)) {
            if (!isExcluded(object.getClass())) {
                return context.getCircularReferenceStrategy().handle(object);
            }

            return object;
        }

        context.getReferences().put(object, Boolean.TRUE);

        if (object instanceof JSONObjectConvertable convertable) {
            return convertable.toJSONObject();
        }

        JSONObject result = new JSONObject();
        Class<?> objectClass = object.getClass();

        if (isExcluded(objectClass)) return object.toString();

        JSONEntity entityAnnotation = objectClass.getAnnotation(JSONEntity.class);
        List<Field> fields = getCachedFields(objectClass);
        if (entityAnnotation != null) {
            String[] excludedFields = entityAnnotation.excluded();
            List<String> excludedFieldList = Arrays.asList(excludedFields);
            fields = fields.stream()
                    .filter(field -> !excludedFieldList.contains(field.getName()))
                    .toList();
        }

        fields = fields.stream()
                .filter(field -> !Modifier.isTransient(field.getModifiers()))
                .toList();

        for (Field field : fields) {
            field.setAccessible(true);

            JSONField annotation = field.getAnnotation(JSONField.class);
            String name = annotation == null
                    ? convert(field.getName())
                    : annotation.name().isEmpty()
                        ? field.getName()
                        : annotation.name();

            Object value;

            try {
                value = field.get(object);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            if (annotation != null) {
                @NotNull JSONType type = annotation.type();

                if (value == null) {
                    result.put(name, null);
                    continue;
                }

                result.put(name, handle(type, field.getType(), value, context));
                continue;
            }

            result.put(name, handle(JSONType.UNSPECIFIED, field.getType(), value, context));
        }

        return result;
    }

    private Object handle(JSONType type, Class<?> fieldType, Object value, MappingContext context) {
        if (value == null) {
            return null;
        }

        if (type == JSONType.UNSPECIFIED) {
            if (Iterable.class.isAssignableFrom(fieldType)
                    || fieldType.isArray()
                    || Map.class.isAssignableFrom(fieldType)) {
                return handleArray(fieldType, value, context);
            }

            return toJSONObjectRecursive(value, context);
        }

        return infer(type, fieldType, value, context);
    }

    private Object infer(JSONType type, Class<?> fieldType, Object value, MappingContext context) {
        return switch (type) {
            case NUMBER      -> handleNumber(value);
            case BOOLEAN     -> handleBoolean(value);
            case STRING      -> handleString(value);
            case ARRAY       -> handleArray(fieldType, value, context);
            case OBJECT      -> toJSONObjectRecursive(value, context);
            case UNSPECIFIED -> throw new AssertionError("Impossible to get here");
        };
    }

    private JSONArray handleArray(Class<?> fieldType, Object value, MappingContext context) {
        JSONArray array = new JSONArray();
        if (fieldType.isArray()) {
            Object[] objects = (Object[]) value;
            for (Object item : objects) {
                array.add(toJSONObjectRecursive(item, context));
            }

            return array;
        }

        if (Map.class.isAssignableFrom(fieldType)) {
            Map<?, ?> map = (Map<?, ?>) value;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                array.add(toJSONObjectRecursive(entry.getKey(), context));
            }

            return array;
        }

        Iterable<?> iterable = (Iterable<?>) value;
        for (Object item : iterable) {
            array.add(toJSONObjectRecursive(item, context));
        }

        return array;
    }

    private String handleString(Object value) {
        if (value instanceof String string) {
            return string;
        }

        return value.toString();
    }

    private Boolean handleBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }

        if (value instanceof String string) {
            return Boolean.parseBoolean(string);
        }

        throw MappingException.of(value.getClass(), Boolean.class);
    }

    private Number handleNumber(Object value) {
        if (value instanceof Number number) {
            return number;
        }

        if (value instanceof String string) {
            return Double.parseDouble(string);
        }

        throw MappingException.of(value.getClass(), Number.class);
    }
}
