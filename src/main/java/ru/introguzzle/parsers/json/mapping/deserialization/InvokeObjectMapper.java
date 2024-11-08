package ru.introguzzle.parsers.json.mapping.deserialization;

import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.cache.CacheService;
import ru.introguzzle.parsers.common.cache.CacheSupplier;
import ru.introguzzle.parsers.json.entity.annotation.JSONField;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.json.mapping.MappingInstantiationException;
import ru.introguzzle.parsers.json.mapping.JSONFieldNameConverter;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.util.function.BiFunction;

public class InvokeObjectMapper extends AbstractObjectMapper {
    private final InstanceSupplier instanceSupplier = new CachingAnnotationInstanceSupplier(this);
    private final FieldNameConverter<JSONField> nameConverter = new JSONFieldNameConverter();

    private static final Cache<Class<?>, Class<?>> ARRAY_TYPE_CACHE;
    private static final Cache<Class<?>, MethodHandle> ARRAY_CONSTRUCTOR_CACHE;
    private static final Cache<Class<?>, MethodHandle> ARRAY_SETTER_CACHE;

    static {
        CacheSupplier service = CacheService.getInstance();

        ARRAY_TYPE_CACHE = service.newCache();
        ARRAY_CONSTRUCTOR_CACHE = service.newCache();
        ARRAY_SETTER_CACHE = service.newCache();
    }


    @SuppressWarnings("unchecked")
    private <T> Class<T[]> getArrayType(Class<T> type) {
        return (Class<T[]>) ARRAY_TYPE_CACHE
                .get(type, t -> Array.newInstance(t, 0).getClass());
    }

    private MethodHandle getArrayConstructorHandle(Class<?> type) {
        return ARRAY_CONSTRUCTOR_CACHE.get(type, MethodHandles::arrayConstructor);
    }

    private MethodHandle getArraySetterHandle(Class<?> type) {
        return ARRAY_SETTER_CACHE.get(type, MethodHandles::arrayElementSetter);
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
    public FieldNameConverter<JSONField> getNameConverter() {
        return nameConverter;
    }
}
