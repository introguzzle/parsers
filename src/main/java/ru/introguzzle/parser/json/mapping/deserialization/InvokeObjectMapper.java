package ru.introguzzle.parser.json.mapping.deserialization;

import ru.introguzzle.parser.common.utilities.NamingUtilities;
import ru.introguzzle.parser.json.mapping.FieldNameConverter;
import ru.introguzzle.parser.json.mapping.MappingInstantiationException;
import ru.introguzzle.parser.json.mapping.ReflectionFieldNameConverter;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class InvokeObjectMapper extends AbstractObjectMapper {
    private final InstanceSupplier instanceSupplier = new CachingAnnotationInstanceSupplier(this);
    private final FieldNameConverter<Field> nameConverter = new ReflectionFieldNameConverter(NamingUtilities::toSnakeCase);

    private static final Map<Class<?>, Class<?>> ARRAY_TYPE_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, MethodHandle> ARRAY_CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, MethodHandle> ARRAY_SETTER_CACHE = new ConcurrentHashMap<>();


    @SuppressWarnings("unchecked")
    private <T> Class<T[]> getArrayType(Class<T> type) {
        return (Class<T[]>) ARRAY_TYPE_CACHE
                .computeIfAbsent(type, t -> Array.newInstance(t, 0).getClass());
    }

    private MethodHandle getArrayConstructorHandle(Class<?> type) {
        return ARRAY_CONSTRUCTOR_CACHE.computeIfAbsent(type, MethodHandles::arrayConstructor);
    }

    private MethodHandle getArraySetterHandle(Class<?> type) {
        return ARRAY_SETTER_CACHE.computeIfAbsent(type, MethodHandles::arrayElementSetter);
    }

    @Override
    protected String getCircularPlaceholder() {
        return "<CIRCULAR_REFERENCE>";
    }

    @Override
    protected InstanceSupplier getInstanceSupplier() {
        return instanceSupplier;
    }

    @Override
    protected BiFunction<Class<?>, Integer, Object> getArraySupplier() {
        return (type, size) -> {
            try {
                return getArrayConstructorHandle(getArrayType(type)).invoke(size);
            } catch (Throwable e) {
                throw new MappingInstantiationException("Can't instantiate array", e);
            }
        };
    }

    @Override
    protected TriConsumer<Object, Integer, Object> getArraySetter() {
        return (array, index, element) -> {
            try {
                getArraySetterHandle(array.getClass())
                        .invokeWithArguments(array, index, element);
            } catch (Throwable e) {
                throw new MappingInstantiationException("Can't set element to array", e);
            }
        };
    }

    @Override
    public FieldNameConverter<Field> getNameConverter() {
        return nameConverter;
    }
}
