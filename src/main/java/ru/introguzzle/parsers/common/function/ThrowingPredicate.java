package ru.introguzzle.parsers.common.function;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Functional interface representing a predicate that can throw a checked exception.
 *
 * <p>This interface is similar to {@link Predicate}, but allows the {@linkplain #test(Object)} method
 * to throw any {@link Throwable}. This can be useful when using predicates that may
 * throw checked exceptions.</p>
 *
 * @param <T> the type of the input to the predicate
 */
@FunctionalInterface
public interface ThrowingPredicate<T> {

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param t the input argument
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
     * @throws Throwable if an error occurs during evaluation
     */
    boolean test(T t) throws Throwable;

    /**
     * Returns a composed {@code ThrowingPredicate} that represents a short-circuiting logical
     * AND of this predicate and another. If this predicate is {@code false}, then the other predicate
     * is not evaluated.
     *
     * @param other a predicate that will be logically-ANDed with this predicate
     * @return a composed {@code ThrowingPredicate} that represents the short-circuiting logical AND
     *         of this predicate and the {@code other} predicate
     * @throws NullPointerException if {@code other} is {@code null}
     */
    default ThrowingPredicate<T> and(ThrowingPredicate<? super T> other) {
        return t -> test(t) && Objects.requireNonNull(other).test(t);
    }

    /**
     * Returns a composed {@code ThrowingPredicate} that represents a short-circuiting logical
     * OR of this predicate and another. If this predicate is {@code true}, then the other predicate
     * is not evaluated.
     *
     * @param other a predicate that will be logically-ORed with this predicate
     * @return a composed {@code ThrowingPredicate} that represents the short-circuiting logical OR
     *         of this predicate and the {@code other} predicate
     * @throws NullPointerException if {@code other} is {@code null}
     */
    default ThrowingPredicate<T> or(ThrowingPredicate<? super T> other) {
        return t -> test(t) || Objects.requireNonNull(other).test(t);
    }

    /**
     * Returns a {@code ThrowingPredicate} that represents the logical negation of this predicate.
     *
     * @return a predicate that represents the logical negation of this predicate
     */
    default ThrowingPredicate<T> negate() {
        return t -> !test(t);
    }

    /**
     * Returns a negated {@code ThrowingPredicate} for the specified target predicate.
     *
     * @param target the predicate to negate
     * @param <T> the type of input to the predicate
     * @return a negated {@code ThrowingPredicate}
     * @throws NullPointerException if {@code target} is {@code null}
     */
    @SuppressWarnings("unchecked")
    static <T> ThrowingPredicate<T> not(ThrowingPredicate<? super T> target) {
        return (ThrowingPredicate<T>) Objects.requireNonNull(target).negate();
    }

    /**
     * Converts this {@code ThrowingPredicate} to a standard {@link Predicate}, transforming any
     * checked exceptions thrown by this predicate into unchecked exceptions using the specified
     * {@link Transformer}.
     *
     * @param handler the transformer that converts checked exceptions to runtime exceptions
     * @return a {@code Predicate} that wraps this predicate and handles checked exceptions
     * @throws NullPointerException if {@code handler} is {@code null}
     */
    default Predicate<T> toPredicate(Transformer<? extends RuntimeException> handler) {
        return t -> {
            try {
                return test(t);
            } catch (Throwable e) {
                throw handler.apply(e);
            }
        };
    }

    /**
     * Converts this {@code ThrowingPredicate} to a standard {@link Predicate}, handling any checked
     * exceptions thrown by this predicate using the specified {@link Handler}. If an
     * exception occurs, the handler is called, and the predicate returns {@code false}.
     *
     * @param handler the exception handler to process checked exceptions
     * @return a {@code Predicate} that wraps this predicate and handles checked exceptions by
     *         returning {@code false} on error
     * @throws NullPointerException if {@code handler} is {@code null}
     */
    default Predicate<T> toPredicate(Handler handler) {
        return t -> {
            try {
                return test(t);
            } catch (Throwable e) {
                handler.accept(e);
                return false;
            }
        };
    }
}
