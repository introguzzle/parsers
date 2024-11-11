package ru.introguzzle.parsers.xml.mapping.deserialization;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.common.field.MethodHandleInvoker;
import ru.introguzzle.parsers.common.field.WritingInvoker;
import ru.introguzzle.parsers.common.mapping.ClassTraverser;
import ru.introguzzle.parsers.common.mapping.Traverser;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeHandler;
import ru.introguzzle.parsers.xml.entity.XMLDocument;
import ru.introguzzle.parsers.xml.entity.annotation.XMLField;
import ru.introguzzle.parsers.xml.mapping.FieldAccessorImpl;
import ru.introguzzle.parsers.xml.mapping.XMLFieldNameConverter;

import java.lang.annotation.Annotation;
import java.util.Map;

public class ObjectMapperImpl implements ObjectMapper {
    private final FieldNameConverter<XMLField> nameConverter = new XMLFieldNameConverter();
    private final FieldAccessor fieldAccessor = new FieldAccessorImpl();
    private final Traverser<Class<?>> traverser = new ClassTraverser();
    private final WritingInvoker writingInvoker = new MethodHandleInvoker.Writing();
    private final ObjectElementMapper elementMapper = new ObjectElementMapperImpl(this);

    @Override
    public <T> @NotNull T toObject(@NotNull XMLDocument document, @NotNull Class<T> type) {
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
        XMLDocument.bindTo(targetType, this);
        return this;
    }

    @Override
    public @NotNull ObjectMapper unbind(@NotNull Class<?> targetType) {
        XMLDocument.unbind(targetType);
        return this;
    }

    @Override
    public @NotNull ObjectMapper unbindAll() {
        XMLDocument.unbindAll();
        return this;
    }

    @Override
    public @NotNull WritingInvoker getWritingInvoker() {
        return writingInvoker;
    }

    @Override
    public @NotNull <T> ObjectMapper withTypeHandler(@NotNull Class<T> type, @NotNull TypeHandler<? extends T> handler) {
        elementMapper.withTypeHandler(type, handler);
        return this;
    }

    @Override
    public @NotNull ObjectMapper withTypeHandlers(@NotNull Map<Class<?>, @NotNull TypeHandler<?>> handlers) {
        elementMapper.withTypeHandlers(handlers);
        return this;
    }

    @Override
    public @NotNull ObjectMapper clearTypeHandlers() {
        elementMapper.clearTypeHandlers();
        return this;
    }
}
