package ru.introguzzle.parsers.common.function;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.util.Nullability;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A functional interface similar to {@link Function}, but allows throwing checked exceptions.
 * <p>
 * The {@code ThrowingFunction} interface represents a function that accepts one argument and produces a result,
 * and can throw any type of {@link Throwable}, including checked exceptions. This is useful when you need to
 * work with lambda expressions or method references that throw checked exceptions.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * ThrowingFunction<String, Integer> parseIntFunction = s -> Integer.parseInt(s);
 *
 * // Using toFunction() to convert to a standard Function that wraps exceptions in RuntimeException
 * Function<String, Integer> safeFunction = parseIntFunction.toFunction();
 *
 * // Using the function
 * try {
 *     Integer result = safeFunction.apply("123");
 * } catch (RuntimeException e) {
 *     // Handle exception
 * }
 * }</pre>
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface ThrowingFunction<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws Throwable if unable to compute a result
     */
    R apply(T t) throws Throwable;

    /**
     * Transforms this throwing function into a standard {@link Function}, wrapping any thrown exceptions
     * into a {@link RuntimeException} using a default transformer.
     *
     * @return a {@link Function} that does not throw checked exceptions
     */
    default @NotNull Function<T, R> asFunction() {
        return asFunction(Transformer.runtime());
    }

    /**
     * Transforms this throwing function into a standard {@link Function}, returning a default value
     * when an exception is thrown.
     * <br>
     * Note: there is no requirement that {@code defaultValue} should be non-null
     *
     * @param defaultValue the default value to return if an exception occurs
     * @return a {@link Function} that returns the default value upon exception
     */
    default @NotNull Function<T, R> asFunction(R defaultValue) {
        return asFunction(() -> defaultValue);
    }

    /**
     * Transforms this throwing function into a standard {@link Function}, using the provided
     * {@link Transformer} to handle exceptions by transforming them into a {@link RuntimeException}.
     *
     * @param transformer the transformer to apply to any thrown exceptions
     * @return a {@link Function} that does not throw checked exceptions
     * @throws NullPointerException if {@code transformer} is null
     */
    default @NotNull Function<T, R> asFunction(@NotNull Transformer<? extends RuntimeException> transformer) {
        return t -> {
            try {
                return apply(t);
            } catch (Throwable e) {
                throw Nullability.requireNonNull(transformer, "transformer").apply(e);
            }
        };
    }

    /**
     * Transforms this throwing function into a standard {@link Function}, using the provided
     * {@link Supplier} to supply a fallback result if an exception is thrown.
     *
     * @param supplier the supplier of the fallback result
     * @return a {@link Function} that returns a fallback result upon exception
     * @throws NullPointerException if {@code supplier} is null
     */
    default @NotNull Function<T, R> asFunction(@NotNull Supplier<? extends R> supplier) {
        return t -> {
            try {
                return apply(t);
            } catch (Throwable e) {
                return Nullability.requireNonNull(supplier, "supplier").get();
            }
        };
    }

    /**
     * Returns a composed {@code ThrowingFunction} that first applies the {@code before} function to its input,
     * and then applies this function to the result.
     *
     * @param <V>    the type of input to the {@code before} function and to the composed function
     * @param before the function to apply before this function is applied
     * @return a composed {@code ThrowingFunction} that first applies the {@code before} function and then applies this function
     * @throws NullPointerException if before is null
     */
    default <V> @NotNull ThrowingFunction<V, R> compose(@NotNull ThrowingFunction<? super V, ? extends T> before) {
        return v -> apply(Nullability.requireNonNull(before, "before").apply(v));
    }

    /**
     * Returns a composed {@code ThrowingFunction} that first applies this function to its input,
     * and then applies the {@code after} function to the result.
     *
     * @param <V>   the type of output of the {@code after} function and of the composed function
     * @param after the function to apply after this function is applied
     * @return a composed {@code ThrowingFunction} that first applies this function and then applies the {@code after} function
     * @throws NullPointerException if after is null
     */
    default <V> @NotNull ThrowingFunction<T, V> andThen(@NotNull ThrowingFunction<? super R, ? extends V> after) {
        return t -> Nullability.requireNonNull(after, "after").apply(apply(t));
    }

    /**
     * Returns a throwing function that always returns its input argument.
     *
     * @param <T> the type of the input and output objects to the function
     * @return a throwing function that always returns its input argument
     */
    static <T> ThrowingFunction<T, T> identity() {
        return t -> t;
    }
}
