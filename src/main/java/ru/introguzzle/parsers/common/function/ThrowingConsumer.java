package ru.introguzzle.parsers.common.function;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.util.Nullability;

import java.util.function.Consumer;

/**
 * A functional interface similar to {@link Consumer}, but allows throwing checked exceptions.
 * <p>
 * The {@code ThrowingConsumer} interface represents an operation that accepts a single input argument
 * and can throw any type of {@link Throwable}, including checked exceptions.
 * This is useful when you need to work with lambda expressions or method references that throw checked exceptions.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * ThrowingConsumer<String> consumer = s -> {
 *     if (s == null) {
 *         throw new Exception("Input cannot be null");
 *     }
 *     System.out.println(s);
 * };
 *
 * // Converting to a standard Consumer that wraps exceptions into RuntimeException
 * Consumer<String> safeConsumer = consumer.toConsumer();
 *
 * // Using the consumer
 * try {
 *     safeConsumer.accept("Hello, World!");
 * } catch (RuntimeException e) {
 *     // Handle exception
 * }
 * }</pre>
 *
 * @param <T> the type of the input to the operation
 */
public interface ThrowingConsumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws Throwable if unable to perform the operation
     */
    void accept(T t) throws Throwable;

    /**
     * Transforms this throwing consumer into a plain {@link Consumer}, wrapping any thrown exceptions
     * into a {@link RuntimeException} using a default transformer.
     *
     * @return a {@link Consumer} that does not throw checked exceptions
     */
    default @NotNull Consumer<T> asConsumer() {
        return asConsumer(Transformer.runtime());
    }

    /**
     * Transforms this throwing consumer into a plain {@link Consumer}, using the provided
     * {@link Transformer} to handle exceptions by transforming them into a {@link RuntimeException}.
     *
     * @param transformer the transformer to apply to any thrown exceptions
     * @return a {@link Consumer} that does not throw checked exceptions
     * @throws NullPointerException if the transformer is null
     */
    default @NotNull Consumer<T> asConsumer(@NotNull Transformer<? extends RuntimeException> transformer) {
        return t -> {
            try {
                accept(t);
            } catch (Throwable e) {
                throw Nullability.requireNonNull(transformer, "transformer").apply(e);
            }
        };
    }

    /**
     * Transforms this throwing consumer into a plain {@link Consumer}, using the provided
     * {@link Handler} to handle exceptions without throwing them.
     *
     * @param handler the handler to process any thrown exceptions
     * @return a {@link Consumer} that handles exceptions using the provided handler
     * @throws NullPointerException if the handler is null
     */
    default @NotNull Consumer<T> asConsumer(@NotNull Handler handler) {
        return t -> {
            try {
                accept(t);
            } catch (Throwable e) {
                Nullability.requireNonNull(handler, "handler").accept(e);
            }
        };
    }

    /**
     * Returns a composed {@code ThrowingConsumer} that performs, in sequence, this operation followed by the {@code after} operation.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code ThrowingConsumer} that performs in sequence this operation followed by the {@code after} operation
     * @throws NullPointerException if after is null
     */
    default @NotNull ThrowingConsumer<T> andThen(@NotNull ThrowingConsumer<? super T> after) {
        return t -> {
            accept(t);
            Nullability.requireNonNull(after, "after").accept(t);
        };
    }
}
