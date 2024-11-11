package ru.introguzzle.parsers.common.mapping;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.field.WritingInvoker;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeHandler;

import java.util.Map;
import java.util.Set;

public interface WritingMapper<M extends WritingMapper<M>> extends Mapper {
    @NotNull M bindTo(@NotNull Class<?> targetType);
    @NotNull M unbind(@NotNull Class<?> targetType);
    @NotNull M unbindAll();

    default WritingMapper<M> bindTo(@NotNull Class<?>[] targetTypes) {
        for (Class<?> targetType : targetTypes) {
            bindTo(targetType);
        }

        return this;
    }

    default WritingMapper<M> bindTo(@NotNull Set<Class<?>> targetTypes) {
        for (Class<?> targetType : targetTypes) {
            bindTo(targetType);
        }

        return this;
    }

    @NotNull WritingInvoker getWritingInvoker();

    <T> @NotNull M withTypeHandler(@NotNull Class<T> type, @NotNull TypeHandler<? extends T> handler);
    @NotNull M withTypeHandlers(@NotNull Map<Class<?>, @NotNull TypeHandler<?>> handlers);
    @NotNull M clearTypeHandlers();
}
