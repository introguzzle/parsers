package ru.introguzzle.parsers.json.mapping.deserialization;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.cache.CacheService;
import ru.introguzzle.parsers.common.cache.CacheSupplier;
import ru.introguzzle.parsers.common.field.MethodHandleInvoker;
import ru.introguzzle.parsers.common.field.WritingInvoker;
import ru.introguzzle.parsers.common.function.TriConsumer;
import ru.introguzzle.parsers.common.mapping.AnnotationData;
import ru.introguzzle.parsers.common.mapping.deserialization.CachingAnnotationInstanceSupplier;
import ru.introguzzle.parsers.common.mapping.deserialization.InstanceSupplier;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.json.entity.annotation.JSONEntity;
import ru.introguzzle.parsers.json.entity.annotation.JSONField;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.json.mapping.JSONFieldNameConverter;
import ru.introguzzle.parsers.json.mapping.JSONMappingException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.util.function.BiFunction;

public class InvokeObjectMapper extends AbstractObjectMapper {
    private final FieldNameConverter<JSONField> nameConverter = new JSONFieldNameConverter();
    private final WritingInvoker writingInvoker = new MethodHandleInvoker.Writing();
    private final AnnotationData<JSONEntity, JSONField> annotationData = new AnnotationData<>(JSONEntity.class, JSONField.class);

    private final InstanceSupplier<JSONObject> instanceSupplier
            = new CachingAnnotationInstanceSupplier<>(annotationData, getFieldAccessor(), getNameConverter(), this::match) {
        private static final Cache<Class<?>, JSONEntity> ANNOTATION_CACHE;

        static {
            CacheSupplier instance = CacheService.instance();
            ANNOTATION_CACHE = instance.newCache();
        }

        @Override
        public Cache<Class<?>, JSONEntity> getAnnotationCache() {
            return ANNOTATION_CACHE;
        }

        @Override
        public ConstructorArgument[] retrieveConstructorArguments(JSONEntity annotation) {
            return annotation.constructorArguments();
        }

        @Override
        public Object retrieveValue(JSONObject object, String name) {
            return object.get(name);
        }
    };

    private static final Cache<Class<?>, Class<?>> ARRAY_TYPE_CACHE;
    private static final Cache<Class<?>, MethodHandle> ARRAY_CONSTRUCTOR_CACHE;
    private static final Cache<Class<?>, MethodHandle> ARRAY_SETTER_CACHE;

    static {
        CacheSupplier supplier = CacheService.instance();

        ARRAY_TYPE_CACHE = supplier.newCache();
        ARRAY_CONSTRUCTOR_CACHE = supplier.newCache();
        ARRAY_SETTER_CACHE = supplier.newCache();
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
    protected @NotNull InstanceSupplier getInstanceSupplier() {
        return instanceSupplier;
    }

    @Override
    protected @NotNull BiFunction<Class<?>, Integer, Object> getArraySupplier() {
        return (type, size) -> {
            try {
                return getArrayConstructorHandle(getArrayType(type)).invoke(size);
            } catch (Throwable e) {
                throw new JSONMappingException("Can't instantiate array", e);
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
                throw new JSONMappingException("Can't set element to array", e);
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
