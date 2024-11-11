package ru.introguzzle.parsers.xml.mapping.deserialization;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.common.field.WritingInvoker;
import ru.introguzzle.parsers.common.mapping.Traverser;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeHandler;
import ru.introguzzle.parsers.xml.entity.XMLElement;

import java.lang.annotation.Annotation;
import java.util.Map;

@RequiredArgsConstructor
public class ObjectElementMapperImpl implements ObjectElementMapper {
    private final ObjectMapper parent;

    @Override
    public <T> @NotNull T toObject(@NotNull XMLElement root, @NotNull Class<T> type) {
        return null;
    }

    @Override
    public @NotNull ObjectElementMapper bindTo(@NotNull Class<?> targetType) {
        parent.bindTo(targetType);
        return this;
    }

    @Override
    public @NotNull ObjectElementMapper unbind(@NotNull Class<?> targetType) {
        parent.unbind(targetType);
        return this;
    }

    @Override
    public @NotNull ObjectElementMapper unbindAll() {
        parent.unbindAll();
        return this;
    }

    @Override
    public @NotNull WritingInvoker getWritingInvoker() {
        return parent.getWritingInvoker();
    }

    @Override
    public @NotNull <T> ObjectElementMapper withTypeHandler(@NotNull Class<T> type, @NotNull TypeHandler<? extends T> handler) {
        return null;
    }

    @Override
    public @NotNull ObjectElementMapper withTypeHandlers(@NotNull Map<Class<?>, @NotNull TypeHandler<?>> handlers) {
        return null;
    }

    @Override
    public @NotNull ObjectElementMapper clearTypeHandlers() {
        return null;
    }

    @Override
    public @NotNull FieldNameConverter<? extends Annotation> getNameConverter() {
        return null;
    }

    @Override
    public @NotNull FieldAccessor getFieldAccessor() {
        return null;
    }

    @Override
    public @NotNull Traverser<Class<?>> getTraverser() {
        return null;
    }
}
