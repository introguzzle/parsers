package ru.introguzzle.parsers.xml.mapping.deserialization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.cache.CacheService;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.common.field.MethodHandleInvoker;
import ru.introguzzle.parsers.common.field.WritingInvoker;
import ru.introguzzle.parsers.common.mapping.ClassTraverser;
import ru.introguzzle.parsers.common.mapping.Traverser;
import ru.introguzzle.parsers.common.mapping.deserialization.InstanceSupplier;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeAdapter;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeResolver;
import ru.introguzzle.parsers.xml.entity.XMLDocument;
import ru.introguzzle.parsers.xml.entity.XMLElement;
import ru.introguzzle.parsers.xml.entity.annotation.XMLEntity;
import ru.introguzzle.parsers.xml.entity.annotation.XMLField;
import ru.introguzzle.parsers.xml.mapping.XMLFieldAccessor;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.BiFunction;

class ObjectMapperImpl implements ObjectMapper {
    private static final MethodHandle BIND_TO_HANDLE, UNBIND_HANDLE, UNBIND_ALL_HANDLE;

    static {
        try {
            Class<?> internal = Class.forName("ru.introguzzle.parsers.xml.entity.Internal");
            Field f = internal.getDeclaredField("BIND_TO_SINGLE");
            f.setAccessible(true);
            BIND_TO_HANDLE = (MethodHandle) f.get(null);

            f = internal.getDeclaredField("UNBIND");
            f.setAccessible(true);
            UNBIND_HANDLE = (MethodHandle) f.get(null);

            f = internal.getDeclaredField("UNBIND_ALL");
            f.setAccessible(true);
            UNBIND_ALL_HANDLE = (MethodHandle) f.get(null);

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private final FieldNameConverter<XMLField> nameConverter;

    private final FieldAccessor fieldAccessor = new XMLFieldAccessor();
    private final Traverser<Class<?>> traverser = new ClassTraverser();
    private final WritingInvoker writingInvoker = new MethodHandleInvoker.Writing();
    private final ObjectElementMapper elementMapper = new ObjectElementMapperImpl(this);
    private final TypeResolver typeResolver = TypeResolver.newResolver(fieldAccessor);
    private final InstanceSupplier<XMLElement> instanceSupplier = InstanceSupplier.getMethodHandleSupplier(
        this, XMLEntity.class, XMLField.class, XMLEntity::constructorArguments, XMLElement::get, ANNOTATION_CACHE);

    private static final Cache<Class<?>, XMLEntity> ANNOTATION_CACHE = CacheService.instance().newCache();

    public ObjectMapperImpl(FieldNameConverter<XMLField> nameConverter) {
        this.nameConverter = nameConverter;
    }

    public @NotNull BiFunction<Object, Type, Object> getForwardCaller() {
        return elementMapper.getForwardCaller();
    }

    @Override
    public @Nullable <T> TypeAdapter<T> findTypeAdapter(@NotNull Class<T> type) {
        return elementMapper.findTypeAdapter(type);
    }

    @Override
    public @NotNull Object toObject(@NotNull XMLDocument document, @NotNull Type type) {
        return elementMapper.toObject(document.getRoot(), type);
    }

    @Override
    public @NotNull FieldNameConverter<? extends Annotation> getNameConverter() {
        return nameConverter;
    }

    @Override
    public @NotNull FieldAccessor getFieldAccessor() {
        return fieldAccessor;
    }

    @Override
    public @NotNull Traverser<Class<?>> getTraverser() {
        return traverser;
    }

    @Override
    public @NotNull ObjectMapper bindTo(@NotNull Class<?> targetType) {
        try {
            BIND_TO_HANDLE.invokeWithArguments(targetType, this);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    @Override
    public @NotNull ObjectMapper unbind(@NotNull Class<?> targetType) {
        try {
            UNBIND_HANDLE.invokeWithArguments(targetType);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    @Override
    public @NotNull ObjectMapper unbindAll() {
        try {
            UNBIND_ALL_HANDLE.invokeWithArguments();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    @Override
    public @NotNull WritingInvoker getWritingInvoker() {
        return writingInvoker;
    }

    @Override
    public @NotNull InstanceSupplier<XMLElement> getInstanceSupplier() {
        return instanceSupplier;
    }

    @Override
    public @NotNull <T> ObjectMapper withTypeAdapter(@NotNull Class<T> type, @NotNull TypeAdapter<? extends T> adapter) {
        elementMapper.withTypeAdapter(type, adapter);
        return this;
    }

    @Override
    public @NotNull ObjectMapper withTypeAdapters(@NotNull Map<Class<?>, @NotNull TypeAdapter<?>> adapters) {
        elementMapper.withTypeAdapters(adapters);
        return this;
    }

    @Override
    public @NotNull ObjectMapper clearTypeAdapters() {
        elementMapper.clearTypeAdapters();
        return this;
    }

    @Override
    public @NotNull TypeResolver getTypeResolver() {
        return typeResolver;
    }
}
