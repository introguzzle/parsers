package ru.introguzzle.parsers.xml.entity;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.xml.mapping.deserialization.ObjectMapper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Set;

@SuppressWarnings("ALL")
class Internal {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final MethodHandle BIND_TO_SINGLE,
            BIND_TO_ARRAY, BIND_TO_SET, UNBIND, UNBIND_ALL;

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
        BIND_TO_SINGLE = mh("bindTo", Class.class, ObjectMapper.class);
        BIND_TO_ARRAY = mh("bindTo", Class[].class, ObjectMapper.class);
        BIND_TO_SET = mh("bindTo", Set.class, ObjectMapper.class);
        UNBIND = mh("unbind", Class.class);
        UNBIND_ALL = mh("unbindAll");
    }

    public static void bindTo(Class<?> type, ObjectMapper mapper) {
        XMLDocument.bindTo(type, mapper);
    }

    public static void bindTo(Class<?>[] types, ObjectMapper mapper) {
        XMLDocument.bindTo(types, mapper);
    }

    public static void bindTo(@NotNull Set<Class<?>> types, @NotNull ObjectMapper mapper) {
        XMLDocument.bindTo(types, mapper);
    }

    public static void unbind(@NotNull Class<?> type) {
        XMLDocument.unbind(type);
    }

    public static void unbindAll() {
        XMLDocument.unbindAll();
    }
}
