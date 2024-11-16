package ru.introguzzle.parsers.json.entity;

import ru.introguzzle.parsers.json.mapping.deserialization.ObjectMapper;
import ru.introguzzle.parsers.json.mapping.serialization.JSONMapper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@SuppressWarnings("ALL")
class Internal {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final MethodHandle SET_PRODUCER, BIND_TO, UNBIND, UNBIND_ALL;

    private static MethodType mt(Class<?>... parameterTypes) {
        return MethodType.methodType(void.class, parameterTypes);
    }

    private static MethodHandle mh(String name, Class<?>... parameterTypes) {
        try {
            return LOOKUP.findStatic(Internal.class, name, mt(parameterTypes));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        SET_PRODUCER = mh("setProducer", JSONObject.class, JSONMapper.class);
        BIND_TO = mh("bindTo", Class.class, ObjectMapper.class);
        UNBIND = mh("unbind", Class.class);
        UNBIND_ALL = mh("unbindAll");
    }

    public static void setProducer(JSONObject object, JSONMapper mapper) {
        object.setProducer(mapper);
    }

    public static void bindTo(Class<?> type, ObjectMapper mapper) {
        JSONObject.bindTo(type, mapper);
    }

    public static void unbind(Class<?> type) {
        JSONObject.unbind(type);
    }

    public static void unbindAll() {
        JSONObject.unbindAll();
    }
}
