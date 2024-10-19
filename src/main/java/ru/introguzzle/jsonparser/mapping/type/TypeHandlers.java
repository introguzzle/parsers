package ru.introguzzle.jsonparser.mapping.type;

import ru.introguzzle.jsonparser.entity.JSONArray;
import ru.introguzzle.jsonparser.entity.JSONObject;
import ru.introguzzle.jsonparser.mapping.Mapper;

import java.util.*;
import java.util.stream.Collectors;

public final class TypeHandlers {
    private static final Map<Class<?>, TypeHandler> HANDLERS = new HashMap<>();
    static {
        HANDLERS.put(Collection.class, (mapper, value, context) -> {
            JSONArray array = new JSONArray();
            Collection<?> collection = (Collection<?>) value;
            array.addAll(collection.stream()
                    .map(o -> mapper.map(o, context))
                    .toList());

            return array;
        });

        HANDLERS.put(Object[].class, (mapper, value, context) -> {
            JSONArray array = new JSONArray();
            if (value == null) {
                return array;
            }

            Object[] objects = (Object[]) value;
            array.addAll(Arrays.stream(objects)
                    .map(o -> mapper.map(o, context))
                    .toList());

            return array;
        });

        HANDLERS.put(Map.class, (mapper, value, context) -> {
            JSONObject object = new JSONObject();
            @SuppressWarnings("unchecked")
            Map<String, ?> map = (Map<String, ?>) value;
            object.putAll(map.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, o -> mapper.map(o, context))
                    )
            );

            return object;
        });

        HANDLERS.put(Boolean.class, (mapper, value, context) -> {
            if (value instanceof Boolean bool) {
                return bool;
            }

            return Boolean.parseBoolean(value.toString());
        });

        HANDLERS.put(Number.class, (mapper, value, context) -> {
            if (value instanceof Number number) {
                return number.doubleValue();
            }

            return Double.parseDouble(value.toString());
        });

        HANDLERS.put(String.class, (mapper, value, context) -> value.toString());
    }

    public static TypeHandler getTypeHandler(Class<?> type) {
        TypeHandler handler = HANDLERS.get(type);
        if (handler != null) {
            return handler;
        }

        for (Class<?> c : HANDLERS.keySet()) {
            if (c.isAssignableFrom(type)) {
                return HANDLERS.get(c);
            }
        }

        if (type.isPrimitive()) {
            if (type == boolean.class) {
                return HANDLERS.get(Boolean.class);
            }

            if (type == int.class
                    || type == double.class
                    || type == long.class
                    || type == byte.class
                    || type == float.class) {
                return HANDLERS.get(Number.class);
            }
        }

        if (type.isArray()) {
            return HANDLERS.get(Object[].class);
        }

        return Mapper::map;
    }
}
