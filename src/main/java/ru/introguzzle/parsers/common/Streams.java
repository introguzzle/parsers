package ru.introguzzle.parsers.common;

import lombok.experimental.UtilityClass;
import ru.introguzzle.parsers.common.function.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
    //
    // casting
    //

    public static <T> Stream<T> select(Stream<T> stream, Class<? extends T> type) {
        return stream.filter(type::isInstance);
    }

    public static <T> Stream<? extends T> downcast(Stream<T> stream, Class<? extends T> type) {
        return stream.filter(type::isInstance).map(type::cast);
    }

    public static <T> Stream<? super T> upcast(Stream<T> stream, Class<? super T> type) {
        return stream.map(type::cast);
    }

    //
    //
    //

    public static <T>
    boolean anyMatchThrowing(Stream<T> stream,
                               ThrowingPredicate<? super T> predicate,
                               Transformer<? extends RuntimeException> handler) {
        return stream.anyMatch(predicate.toPredicate(handler));
    }

    public static <T>
    boolean allMatchThrowing(Stream<T> stream,
                             ThrowingPredicate<? super T> predicate,
                             Transformer<? extends RuntimeException> handler) {
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
                             Transformer<? extends RuntimeException> transformer) {
        return stream.filter(predicate.toPredicate(transformer));
    }

    public static <T>
    Stream<T> filterThrowing(Stream<T> stream,
                             ThrowingPredicate<? super T> predicate,
                             Handler handler) {
        return stream.filter(predicate.toPredicate(handler));
    }

    public static <T>
    Stream<T> rejectThrowing(Stream<T> stream,
                             ThrowingPredicate<? super T> predicate,
                             Transformer<? extends RuntimeException> transformer) {
        return stream.filter(predicate.negate().toPredicate(transformer));
    }

    public static <T>
    Stream<T> rejectThrowing(Stream<T> stream,
                             ThrowingPredicate<? super T> predicate,
                             Handler handler) {
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
                          Transformer<? extends RuntimeException> transformer) {
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
                              Transformer<? extends RuntimeException> transformer) {
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
}