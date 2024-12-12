package ru.introguzzle.parsers.xml.mapping.deserialization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.cache.CacheService;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.common.field.MethodHandleInvoker;
import ru.introguzzle.parsers.common.field.WritingInvoker;
import ru.introguzzle.parsers.common.mapping.AnnotationData;
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
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.BiFunction;

class ObjectMapperImpl implements ObjectMapper {
    private final FieldNameConverter<XMLField> nameConverter;

    private final FieldAccessor fieldAccessor = new XMLFieldAccessor();
    private final Traverser<Class<?>> traverser = new ClassTraverser();
    private final WritingInvoker writingInvoker = new MethodHandleInvoker.Writing();
    private final ObjectElementMapper elementMapper = new ObjectElementMapperImpl(this);
    private final TypeResolver typeResolver = TypeResolver.newResolver(fieldAccessor);
    private final InstanceSupplier<XMLElement> instanceSupplier = InstanceSupplier.getMethodHandleSupplier(
        this, AnnotationData.XML, XMLEntity::constructorArguments, XMLElement::get, ANNOTATION_CACHE);

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
    @SuppressWarnings("unchecked")
    public @NotNull <T> T toObject(@NotNull XMLDocument document, @NotNull Type type) {
        return (T) elementMapper.toObject(document.getRoot(), type);
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
