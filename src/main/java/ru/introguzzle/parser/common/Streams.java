package ru.introguzzle.parser.common;

import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.*;

/**
 * Utility class providing extended methods for working with {@link Stream}.
 * <p>
 * This class is intended to be used in conjunction with the {@link lombok.experimental.ExtensionMethod} annotation,
 * allowing additional methods to be applied directly to {@link Stream} objects.
 * <p>
 * The main features of the class include:
 * <ul>
 *     <li>Support for functional interfaces capable of throwing exceptions.</li>
 *     <li>Methods for processing streams with the ability to transform or handle exceptions.</li>
 *     <li>Additional methods for filtering, transforming, and aggregating data in streams.</li>
 * </ul>
 * <p>
 * <strong>Example usage:</strong>
 * <pre>{@code
 * import static ru.introguzzle.parser.common.Streams.*;
 *
 * List<String> result = stream
 *     .filterThrowing(s -> throw new Exception())
 *     .filterThrowing(Objects::nonNull)
 *     .toList();
 *
 * assert result.isEmpty();
 * }</pre>
 *
 * <p>
 * The class contains the following main components:
 * <ul>
 *     <li>Internal functional interfaces for handling exceptions in lambda expressions.</li>
 *     <li>Methods that extend the standard {@link Stream} functionality, allowing working with functions that can throw exceptions.</li>
 * </ul>
 *
 * @see Stream
 * @see lombok.experimental.ExtensionMethod
 */

@SuppressWarnings("unused")
@UtilityClass
public final class Streams {
    @FunctionalInterface
    public interface ExceptionTransformer<E extends RuntimeException> extends Function<Throwable, E> {

    }

    @FunctionalInterface
    public interface ExceptionHandler extends Consumer<Throwable> {

    }

    public interface ThrowingPredicate<T> {
        boolean test(T t) throws Throwable;

        default ThrowingPredicate<T> and(ThrowingPredicate<? super T> other) {
            Objects.requireNonNull(other);
            return t -> test(t) && other.test(t);
        }

        default ThrowingPredicate<T> or(ThrowingPredicate<? super T> other) {
            Objects.requireNonNull(other);
            return t -> test(t) || other.test(t);
        }

        default ThrowingPredicate<T> negate() {
            return t -> !test(t);
        }

        @SuppressWarnings("unchecked")
        static <T> ThrowingPredicate<T> not(ThrowingPredicate<? super T> target) {
            Objects.requireNonNull(target);
            return (ThrowingPredicate<T>) target.negate();
        }

        default Predicate<T> toPredicate(ExceptionTransformer<? extends RuntimeException> handler) {
            return t -> {
                try {
                    return test(t);
                } catch (Throwable e) {
                    throw handler.apply(e);
                }
            };
        }

        default Predicate<T> toPredicate(ExceptionHandler handler) {
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

    public interface ThrowingConsumer<T> {
        void accept(T t) throws Throwable;

        default Consumer<T> toConsumer() {
            return t -> {
                try {
                    accept(t);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            };
        }

        default ThrowingConsumer<T> andThen(ThrowingConsumer<? super T> after) {
            Objects.requireNonNull(after);
            return t -> {
                accept(t);
                after.accept(t);
            };
        }
    }

    public static <T>
    boolean anyMatchThrowing(Stream<T> stream,
                               ThrowingPredicate<? super T> predicate,
                               ExceptionTransformer<? extends RuntimeException> handler) {
        return stream.anyMatch(predicate.toPredicate(handler));
    }

    public static <T>
    boolean allMatchThrowing(Stream<T> stream,
                             ThrowingPredicate<? super T> predicate,
                             ExceptionTransformer<? extends RuntimeException> handler) {
        return stream.allMatch(predicate.toPredicate(handler));
    }

    public static <T>
    void forEachThrowing(Stream<T> stream, ThrowingConsumer<T> consumer) {
        stream.forEach(consumer.toConsumer());
    }

    public static <T>
    Stream<T> peekThrowing(Stream<T> stream, ThrowingConsumer<? super T> consumer) {
        return stream.peek(consumer.toConsumer());
    }

    public static <T>
    Stream<T> reject(Stream<T> stream,
                     Predicate<? super T> predicate) {
        return stream.filter(predicate.negate());
    }

    public static <T>
    Stream<T> filterThrowing(Stream<T> stream,
                             ThrowingPredicate<? super T> predicate,
                             ExceptionTransformer<? extends RuntimeException> transformer) {
        return stream.filter(predicate.toPredicate(transformer));
    }

    public static <T>
    Stream<T> filterThrowing(Stream<T> stream,
                             ThrowingPredicate<? super T> predicate,
                             ExceptionHandler handler) {
        return stream.filter(predicate.toPredicate(handler));
    }

    public static <T>
    Stream<T> rejectThrowing(Stream<T> stream,
                             ThrowingPredicate<? super T> predicate,
                             ExceptionTransformer<? extends RuntimeException> transformer) {
        return stream.filter(predicate.negate().toPredicate(transformer));
    }

    public static <T>
    Stream<T> rejectThrowing(Stream<T> stream,
                             ThrowingPredicate<? super T> predicate,
                             ExceptionHandler handler) {
        return stream.filter(predicate.negate().toPredicate(handler));
    }

    public static <T> Set<T> toSet(Stream<T> stream) {
        return stream.collect(Collectors.toSet());
    }

    public static <T, K, V>
    Map<K, V> toMap(Stream<T> stream,
                    Function<? super T, ? extends K> keyMapper,
                    Function<? super T, ? extends V> valueMapper) {
        return stream.collect(Collectors.toMap(keyMapper, valueMapper));
    }

    public static <T, K>
    Map<K, List<T>> groupBy(Stream<T> stream,
                            Function<? super T, ? extends K> classifier) {
        return stream.collect(Collectors.groupingBy(classifier));
    }

    public interface ThrowingFunction<T, R> {
        R apply(T t) throws Throwable;

        default Function<T, R> toFunction() {
            return toFunction(() -> (R) null);
        }

        default Function<T, R> toFunction(R defaultValue) {
            return toFunction(() -> defaultValue);
        }

        default Function<T, R> toFunction(ExceptionTransformer<? extends RuntimeException> transformer) {
            return t -> {
                try {
                    return apply(t);
                } catch (Throwable e) {
                    throw transformer.apply(e);
                }
            };
        }

        default Function<T, R> toFunction(Supplier<? extends R> supplier) {
            return t -> {
                try {
                    return apply(t);
                } catch (Throwable e) {
                    return supplier.get();
                }
            };
        }

        default <V> ThrowingFunction<V, R> compose(ThrowingFunction<? super V, ? extends T> before) {
            Objects.requireNonNull(before);
            return (V v) -> apply(before.apply(v));
        }

        default <V> ThrowingFunction<T, V> andThen(ThrowingFunction<? super R, ? extends V> after) {
            Objects.requireNonNull(after);
            return (T t) -> after.apply(apply(t));
        }

        static <T> ThrowingFunction<T, T> identity() {
            return t -> t;
        }
    }

    //
    // map
    //

    public static <T, R>
    Stream<R> mapThrowing(Stream<T> stream,
                          ThrowingFunction<? super T, ? extends R> mapper) {
        return stream.map(mapper.toFunction());
    }

    public static <T, R>
    Stream<R> mapThrowing(Stream<T> stream,
                          ThrowingFunction<? super T, ? extends R> mapper,
                          ExceptionTransformer<? extends RuntimeException> transformer) {
        return stream.map(mapper.toFunction(transformer));
    }

    public static <T, R>
    Stream<R> mapThrowing(Stream<T> stream,
                          ThrowingFunction<? super T, ? extends R> mapper,
                          R defaultValue) {
        return stream.map(t -> {
            try {
                return mapper.apply(t);
            } catch (Throwable e) {
                return defaultValue;
            }
        });
    }

    //
    // flatMap
    //


    public static <T, R>
    Stream<R> flatMapThrowing(Stream<T> stream,
                              ThrowingFunction<? super T, ? extends Stream<R>> mapper) {
        return stream.flatMap(mapper.toFunction());
    }

    public static <T, R>
    Stream<R> flatMapThrowing(Stream<T> stream,
                              ThrowingFunction<? super T, ? extends Stream<R>> mapper,
                              ExceptionTransformer<? extends RuntimeException> transformer) {
        return stream.flatMap(mapper.toFunction(transformer));
    }

    //
    // concat
    //

    public static <T> Stream<T> append(Stream<? extends T> stream, Stream<? extends T> other) {
        return Stream.concat(stream, other);
    }

    public static <T> Stream<T> append(Stream<? extends T> stream, Iterable<? extends T> iterable) {
        return Stream.concat(stream, StreamSupport.stream(iterable.spliterator(), stream.isParallel()));
    }

    public static <T> Stream<T> append(Stream<? extends T> stream, T... elements) {
        return Stream.concat(stream, Stream.of(elements));
    }

    //
    // array
    //

    public static IntStream stream(int[] array) {
        return Arrays.stream(array);
    }

    public static IntStream stream(int[] array, int startInclusive, int endExclusive) {
        return Arrays.stream(array, startInclusive, endExclusive);
    }

    public static LongStream stream(long[] array) {
        return Arrays.stream(array);
    }

    public static LongStream stream(long[] array, int startInclusive, int endExclusive) {
        return Arrays.stream(array, startInclusive, endExclusive);
    }

    public static DoubleStream stream(double[] array) {
        return Arrays.stream(array);
    }

    public static DoubleStream stream(double[] array, int startInclusive, int endExclusive) {
        return Arrays.stream(array, startInclusive, endExclusive);
    }

    public static Stream<Float> stream(float[] array) {
        return stream(array, 0, array.length);
    }

    public static Stream<Float> stream(float[] array, int startInclusive, int endExclusive) {
        List<Float> list = new ArrayList<>();
        for (int i = startInclusive; i < endExclusive; i++) {
            list.add(array[i]);
        }

        return list.stream();
    }

    public static Stream<Boolean> stream(boolean[] array) {
        return stream(array, 0, array.length);
    }

    public static Stream<Boolean> stream(boolean[] array, int startInclusive, int endExclusive) {
        List<Boolean> list = new ArrayList<>();
        for (int i = startInclusive; i < endExclusive; i++) {
            list.add(array[i]);
        }

        return list.stream();
    }

    public static Stream<Character> stream(char[] array) {
        return stream(array, 0, array.length);
    }

    public static Stream<Character> stream(char[] array, int startInclusive, int endExclusive) {
        List<Character> list = new ArrayList<>();
        for (int i = startInclusive; i < endExclusive; i++) {
            list.add(array[i]);
        }

        return list.stream();
    }
}