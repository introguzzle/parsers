package ru.introguzzle.parsers.common.mapping;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.common.field.WritingInvoker;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeAdapter;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeResolver;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

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
    @NotNull BiFunction<Object, Type, Object> getForwardCaller();

    <T> @Nullable TypeAdapter<T> findTypeAdapter(@NotNull Class<T> type);
    <T> @NotNull M withTypeAdapter(@NotNull Class<T> type, @NotNull TypeAdapter<? extends T> adapter);
    @NotNull M withTypeAdapters(@NotNull Map<Class<?>, @NotNull TypeAdapter<?>> adapters);
    @NotNull M clearTypeAdapters();

    @NotNull TypeResolver getTypeResolver();
}
