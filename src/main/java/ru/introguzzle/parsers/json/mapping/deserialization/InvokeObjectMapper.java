package ru.introguzzle.parsers.json.mapping.deserialization;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.cache.CacheService;
import ru.introguzzle.parsers.common.cache.CacheSupplier;
import ru.introguzzle.parsers.common.field.MethodHandleInvoker;
import ru.introguzzle.parsers.common.field.WritingInvoker;
import ru.introguzzle.parsers.common.function.TriConsumer;
import ru.introguzzle.parsers.common.mapping.AnnotationData;
import ru.introguzzle.parsers.common.mapping.MappingException;
import ru.introguzzle.parsers.common.mapping.deserialization.InstanceSupplier;
import ru.introguzzle.parsers.common.util.DelegatingMap;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.json.entity.annotation.JSONEntity;
import ru.introguzzle.parsers.json.entity.annotation.JSONField;
import ru.introguzzle.parsers.common.field.FieldNameConverter;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.function.BiFunction;

@RequiredArgsConstructor
class InvokeObjectMapper extends AbstractObjectMapper {
    private final FieldNameConverter<JSONField> nameConverter;

    private final WritingInvoker writingInvoker = new MethodHandleInvoker.Writing();
    private final InstanceSupplier<JSONObject> instanceSupplier = InstanceSupplier.getMethodHandleSupplier(
            this, AnnotationData.JSON, JSONEntity::constructorArguments, DelegatingMap::get, ANNOTATION_CACHE);

    private static final Cache<Class<?>, Class<?>> ARRAY_TYPE_CACHE;
    private static final Cache<Class<?>, MethodHandle> ARRAY_CONSTRUCTOR_CACHE;
    private static final Cache<Class<?>, MethodHandle> ARRAY_SETTER_CACHE;
    private static final Cache<Class<?>, JSONEntity> ANNOTATION_CACHE;

    static {
        CacheSupplier supplier = CacheService.instance();

        ARRAY_TYPE_CACHE = supplier.newCache();
        ARRAY_CONSTRUCTOR_CACHE = supplier.newCache();
        ARRAY_SETTER_CACHE = supplier.newCache();
        ANNOTATION_CACHE = supplier.newCache();
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
    protected @NotNull String getCircularPlaceholder() {
        return "<CIRCULAR_REFERENCE>";
    }

    @Override
    public @NotNull InstanceSupplier<JSONObject> getInstanceSupplier() {
        return instanceSupplier;
    }

    @Override
    protected @NotNull BiFunction<Type, Integer, Object> getArraySupplier() {
        return (type, size) -> {
            try {
                Class<?> raw = getTypeResolver().getRawType(type);
                return getArrayConstructorHandle(getArrayType(raw)).invoke(size);
            } catch (Throwable e) {
                throw new MappingException("Failed to instantiate array", e);
            }
        };
    }

    @Override
    protected @NotNull TriConsumer<Object, Integer, Object> getArraySetter() {
        return (array, index, element) -> {
            try {
                getArraySetterHandle(array.getClass())
                        .invokeWithArguments(array, index, element);
            } catch (Throwable e) {
                throw new MappingException("Failed to set element to array", e);
            }
        };
    }

    @Override
    public @NotNull FieldNameConverter<JSONField> getNameConverter() {
        return nameConverter;
    }

    @Override
    public @NotNull WritingInvoker getWritingInvoker() {
        return writingInvoker;
    }
}
