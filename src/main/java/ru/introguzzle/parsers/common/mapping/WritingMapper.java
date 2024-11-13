package ru.introguzzle.parsers.common.mapping;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.field.GenericTypeAccessor;
import ru.introguzzle.parsers.common.field.WritingInvoker;
import ru.introguzzle.parsers.common.function.TriFunction;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeHandler;

import java.util.List;
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
    @NotNull GenericTypeAccessor getGenericTypeAccessor();

    /**
     * Retrieves the forward caller function used for type conversions.
     *
     * <p>The {@code BiFunction} returned by this method is used to convert objects from one type to another
     * during the deserialization process. It takes an {@code Object} as input and the target {@code Class},
     * then returns an instance of the target type.</p>
     *
     * @return A {@code BiFunction} that performs type conversions, taking an {@code Object} and a target {@code Class},
     * and returning an instance of the target type.
     */
    @NotNull TriFunction<Object, Class<?>, List<Class<?>>, Object> getForwardCaller();

    <T> @NotNull M withTypeHandler(@NotNull Class<T> type, @NotNull TypeHandler<? extends T> handler);
    @NotNull M withTypeHandlers(@NotNull Map<Class<?>, @NotNull TypeHandler<?>> handlers);
    @NotNull M clearTypeHandlers();
}
