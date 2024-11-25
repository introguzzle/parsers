package ru.introguzzle.parsers.common.util;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Class that delegates all functionality to underlying actual implementation of {@link List}.
 * Provides additional methods to operate
 * @param <T> the type of elements in this list
 */
@SuppressWarnings("unused")
public class DelegatingArray<T> implements List<T> {

    /**
     * Delegate list that backs up this DelegatingArray
     */
    protected final List<T> list;

    /**
     * Constructs a new empty DelegatingArray with {@link ArrayList} as delegate
     */
    public DelegatingArray() {
        list = new ArrayList<>();
    }

    /**
     * Constructs a new DelegatingArray with {@link ArrayList} as delegate
     * that contains elements of {@code collection}
     * @param collection collection
     */
    public DelegatingArray(@NotNull Collection<? extends T> collection) {
        list = new ArrayList<>(collection);
    }

    /**
     * Constructs a new DelegatingArray with {@link ArrayList} as delegate
     * that contains elements of {@code array}
     * @param array array
     */
    public DelegatingArray(@NotNull T[] array) {
        list = new ArrayList<>(List.of(array));
    }

    /**
     * Constructs a new DelegatingArray with specified {@code l} as {@link List} to delegate
     * @param l delegate
     */
    @SuppressWarnings("unchecked")
    public DelegatingArray(@NotNull List<? extends T> l) {
        list = (List<T>) l;
    }

    /**
     * Copying constructor
     * Resulting constructed DelegatingArray will be backed up by {@link ArrayList}
     * @param d array to copy from
     */
    public DelegatingArray(DelegatingArray<? extends T> d) {
        list = new ArrayList<>(d.list);
    }

    /**
     * Retrieves class of actual underlying delegate list
     * @return class of underlying delegate list
     */
    @SuppressWarnings("unchecked")
    public Class<? extends List<T>> getImplementationClass() {
        return (Class<? extends List<T>>) (list instanceof DelegatingArray && !list.getClass().isAnonymousClass()
                ? ((DelegatingArray<T>) list).getImplementationClass()
                : list.getClass());
    }

    /**
     * Returns {@code true} if this list contains the specified element.
     * More formally, returns {@code true} if and only if this list contains
     * at least one element {@code e} such that
     * {@code Objects.equals(o, e)}.
     *
     * @param item element whose presence in this list is to be tested
     * @return {@code true} if this list contains the specified element
     */
    @SuppressWarnings("ALL")
    public boolean has(Object item) {
        return list.contains(item);
    }

    /**
     * Returns the index of the first occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the lowest index {@code i} such that
     * {@code Objects.equals(o, get(i))},
     * or -1 if there is no such index.
     *
     * @param target element to search for
     * @return the index of the first occurrence of the specified element in
     *         this list, or -1 if this list does not contain the element
     *
     */
    public int search(Object target) {
        return indexOf(target);
    }

    /**
     * Returns the index of the first occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the lowest index {@code i} such that
     * {@code target == i},
     * or -1 if there is no such index.
     *
     * @param target element to search for
     * @return the index of the first occurrence of the specified element in
     *         this list, or -1 if this list does not contain the element
     *
     */
    public int referenceSearch(Object target) {
        if (target == null) return search(null);

        int size = size();
        for (int i = 0; i < size; i++) {
            if (get(i) == target) return i;
        }

        return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the highest index {@code i} such that
     * {@code Objects.equals(o, get(i))},
     * or -1 if there is no such index.
     * @param target element to search for
     * @return the index of the last occurrence of the specified element in
     *         this list, or -1 if this list does not contain the element
     */
    public int searchReversed(Object target) {
        return lastIndexOf(target);
    }

    /**
     * Returns the index of the last occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the highest index {@code i} such that
     * {@code target == i},
     * or -1 if there is no such index.
     * @param target element to search for
     * @return the index of the last occurrence of the specified element in
     *         this list, or -1 if this list does not contain the element
     */
    public int referenceSearchReversed(Object target) {
        if (target == null) return searchReversed(null);

        int size = size();
        for (int i = size - 1; i >= 0; i--) {
            if (get(i) == target) return i;
        }

        return -1;
    }

    /**
     * Returns an enumeration over this array. This provides
     * interoperability with legacy APIs that require an enumeration
     * as input.
     *
     * <p>The iterator returned from a call to {@link Enumeration#asIterator()}
     * does not support removal of elements from this array. This
     * is necessary to avoid unintentionally increasing the capabilities of the
     * returned enumeration.
     *
     * @return an enumeration over this array
     * @see Enumeration
     */
    public Enumeration<T> enumeration() {
        return Collections.enumeration(this);
    }

    /**
     * Sets specified {@code element} for all elements of this array.
     * Semantics are the same as {@linkplain Collections#fill(List, Object)}
     *
     * @param element element to be set
     * @param <U> type of element
     */
    public <U extends T> void setAll(U element) {
        Collections.fill(this, element);
    }

    /**
     * Swaps the elements at the specified positions in this array
     * (If the specified positions are equal, invoking this method leaves
     * the array unchanged.).
     * Semantics are the same as {@linkplain Collections#swap(List, int, int)}
     *
     * @param i the index of one element to be swapped.
     * @param j the index of the other element to be swapped.
     * @throws IndexOutOfBoundsException if either {@code i} or {@code j}
     *         is out of range (i &lt; 0 || i &gt;= array.size()
     *         || j &lt; 0 || j &gt;= array.size()).
     * @since 1.4
     */
    public void swap(int i, int j) {
        if (i == j) return;

        Collections.swap(this, i, j);
    }

    /**
     * Creates union of specified {@code list} and this array
     * @param list list to be joined
     * @param supplier supplier that supplies empty list of subtype of {@link List}.
     *                 Usually it's method reference (e.g {@code ArrayList::new})
     * @return union that  contains all elements of specified {@code list}
     * and all elements of this array
     * @param <R> type of resulting list
     * @throws IllegalArgumentException if {@code supplier} supplies non-empty {@link List}
     */
    public <R extends List<T>> R and(List<? extends T> list, Supplier<? extends R> supplier) {
        R a = asRequiringEmpty(supplier).get();
        a.addAll(this);
        a.addAll(list);

        return a;
    }

    /**
     * Creates union of specified {@code list} and this array
     * @param list list to be joined
     * @return union of type {@link DelegatingArray} that contains all elements of specified {@code list}
     * and all elements of this array
     */
    public DelegatingArray<T> union(List<? extends T> list) {
        return and(list, DelegatingArray::new);
    }

    /**
     * Creates intersection (logical OR) of specified {@code list} and this array.
     * More formally, creates a new list of type {@code R} that it contains only those
     * elements that are contained in both {@code list} and this array
     * @param list list to be intersected
     * @param supplier supplier that supplies empty list of subtype of {@link List}.
     *                 Usually it's method reference (e.g {@code ArrayList::new})
     * @return intersection of specified {@code list} and this array
     * @throws IllegalArgumentException if {@code supplier} supplies non-empty {@link List}
     * @param <R> type of resulting list
     */
    public <R extends List<T>> R or(List<? extends T> list, Supplier<? extends R> supplier) {
        R result = asRequiringEmpty(supplier).get();
        result.addAll(this);
        result.retainAll(list);

        return result;
    }

    /**
     * Creates intersection (logical OR) of specified {@code list} and this array.
     * More formally, creates a new list of type {@code R} that it contains only those
     * elements that are contained in both {@code list} and this array
     * @param list list to be intersected
     * @return intersection of type {@link DelegatingArray} of specified {@code list} and this array
     */
    public DelegatingArray<T> intersection(List<? extends T> list) {
        return or(list, DelegatingArray::new);
    }

    /**
     * Creates union of specified {@code set} and this array
     * @param set set to be joined
     * @param supplier supplier that supplies empty set of subtype of {@link Set}.
     *                 Usually it's method reference (e.g {@code HashSet::new})
     * @return union that contains only unique elements (according to {@link Set} semantics)
     * of all unique elements of specified {@code set}
     * and all unique elements of this array
     * @throws IllegalArgumentException if {@code supplier} supplies non-empty {@link Set}
     * @param <R> type of resulting set
     */
    public <R extends Set<T>> R and(Set<? extends T> set, Supplier<? extends R> supplier) {
        R result = asRequiringEmpty(supplier).get();
        result.addAll(this);
        result.addAll(set);

        return result;
    }

    /**
     * Creates union of specified {@code set} and this array
     * @param set set to be joined
     * @return union of type {@link LinkedHashSet} that contains only unique elements (according to {@link Set} semantics)
     * of all unique elements of specified {@code set}
     * and all unique elements of this array
     */
    public Set<T> union(Set<? extends T> set) {
        return and(set, LinkedHashSet::new);
    }

    /**
     * Creates intersection (logical OR) of specified {@code set} and this array.
     * More formally, creates a new set of type {@code R} that it contains only those unique
     * elements that are contained in both {@code set} and this array
     * @param set list to be intersected
     * @param supplier supplier that supplies empty list of subtype of {@link Set}.
     *                 Usually it's method reference (e.g {@code HashSet::new})
     * @return intersection of specified {@code list} and this array
     * @throws IllegalArgumentException if {@code supplier} supplies non-empty {@link Set}
     * @param <R> type of resulting list
     */
    public <R extends Set<T>> R or(Set<? extends T> set, Supplier<? extends R> supplier) {
        R result = asRequiringEmpty(supplier).get();
        result.addAll(this);
        result.retainAll(set);

        return result;
    }

    /**
     * Creates intersection (logical OR) of specified {@code set} and this array.
     * More formally, creates a new set of type {@code R} that it contains only those unique
     * elements that are contained in both {@code set} and this array
     * @param set list to be intersected
     * @return intersection of type {@link LinkedHashSet} of specified {@code list} and this array
     */
    public Set<T> intersection(Set<? extends T> set) {
        return or(set, LinkedHashSet::new);
    }

    /**
     * Returns {@code true} if the specified {@code collection} and this array have no
     * elements in common.
     * Semantics are the same as {@linkplain Collections#disjoint(Collection, Collection)}
     *
     * @param collection a collection
     * @return {@code true} if the two specified collections have no
     * elements in common, {@code false} otherwise
     */
    public boolean disjoint(Collection<? extends T> collection) {
        return Collections.disjoint(this, collection);
    }

    /**
     * Returns the maximum element of the given collection, according to the
     * order induced by the specified comparator.  All elements in the
     * collection must be <i>mutually comparable</i> by the specified
     * comparator (that is, {@code comp.compare(e1, e2)} must not throw a
     * {@code ClassCastException} for any elements {@code e1} and
     * {@code e2} in the collection).<p>
     * Semantics are the same as {@linkplain Collections#max(Collection)}
     *
     * @param  comparator the comparator with which to determine the maximum element.
     *         A {@code null} value indicates that the elements' <i>natural
     *        ordering</i> should be used.
     * @return the maximum element of the given collection, according
     *         to the specified comparator.
     */
    public T max(Comparator<? super T> comparator) {
        return Collections.max(this, comparator);
    }

    /**
     * Returns the minimum element of the given collection, according to the
     * order induced by the specified comparator.  All elements in the
     * collection must be <i>mutually comparable</i> by the specified
     * comparator (that is, {@code comp.compare(e1, e2)} must not throw a
     * {@code ClassCastException} for any elements {@code e1} and
     * {@code e2} in the collection).<p>
     * Semantics are the same as {@linkplain Collections#min(Collection)}
     *
     * @param  comparator the comparator with which to determine the minimum element.
     *         A {@code null} value indicates that the elements' <i>natural
     *        ordering</i> should be used.
     * @return the minimal element of the given collection, according
     *         to the specified comparator.
     */
    public T min(Comparator<? super T> comparator) {
        return Collections.min(this, comparator);
    }

    /**
     * Returns the number of elements in this array equal to the
     * specified {@code target}. More formally, returns the number of elements
     * {@code e} in the collection such that
     * {@code Objects.equals(target, e)}.
     *
     * @param target the object whose frequency is to be determined
     * @return the number of elements in this array equal to {@code o}
     */
    public int frequency(Object target) {
        return Collections.frequency(this, target);
    }

    /**
     * Returns the number of elements in this array equal by reference to the
     * specified {@code target}. More formally, returns the number of elements
     * {@code e} in the collection such that
     * {@code target == e}.
     *
     * @param target the object whose frequency is to be determined
     * @return the number of elements in this array equal by reference to {@code target}
     */
    public int referenceFrequency(Object target) {
        int size = size();
        int count = 0;
        for (int i = 0; i < size; i++) {
            if (get(i) == target) count++;
        }

        return count;
    }

    /**
     * Creates a shallow copy of this array using copying constructor
     * @return shallow copy of this array
     */
    public DelegatingArray<T> copy() {
        return new DelegatingArray<>(this);
    }

    /**
     * Performs reducing operation on all elements of this array
     * @param identity initial value
     * @param accumulator function that accepts reduced value and element of this array,
     *                    and returns result of reducing operation
     * @return reduced value
     * @param <R> type of result of reducing operation
     */
    public <R> R reduce(R identity, BiFunction<? super R, ? super T, ? extends R> accumulator) {
        R result = identity;

        for (T element : this) {
            result = accumulator.apply(result, element);
        }

        return result;
    }

    /**
     * Creates union of type {@link DelegatingArray} of this array and {@code times} copy of this array.
     * Returns itself when {@code times <= 0}
     * @param times times to repeat
     * @return union of this array and {@code times} copy of this array.
     * Size of resulting array will be: {@code size() + size() * times}
     */
    public DelegatingArray<T> repeat(int times) {
        return repeat(times, DelegatingArray::new);
    }

    /**
     * Creates union of type {@link DelegatingArray} of this array and {@code times} copy of this array.
     * Returns itself when {@code times <= 0}
     * @param times times to repeat
     * @param supplier supplier that supplies empty list of subtype of {@link List}.
     *                 Usually it's method reference (e.g {@code ArrayList::new})
     * @param <R> type of resulting list
     * @return union of type {@code R} of this array and {@code times} copy of this array.
     * Size of resulting array will be: {@code size() + size() * times}
     */
    @SuppressWarnings("ALL")
    public <R extends List<T>> R repeat(int times, Supplier<? extends R> supplier) {
        R result = asRequiringEmpty(supplier).get();
        if (times <= 0) {
            return result;
        }

        result.addAll(this);

        for (int i = 0; i < times; i++) {
            result.addAll(result);
        }

        return result;
    }

    /**
     * Creates a map of frequencies using equals comparison based on {@link LinkedHashMap}
     * that has keys as values of this array and values as frequency counts
     * of corresponding values
     * @return {@link LinkedHashMap} of frequencies
     */
    public Map<T, Integer> counter() {
        return counter(LinkedHashMap::new);
    }

    /**
     * Creates a map of frequencies using reference comparison based on {@link LinkedHashMap}
     * that has keys as values of this array and values as frequency counts
     * of corresponding values
     * @return {@link LinkedHashMap} of reference frequencies
     */
    public Map<T, Integer> referenceCounter() {
        return referenceCounter(LinkedHashMap::new);
    }

    /**
     * Creates a map of frequencies using reference comparison based on {@link LinkedHashMap}
     * that has keys as values of this array and values as frequency counts
     * of corresponding values
     * @param supplier supplier that supplies empty map of subtype of {@link Map}.
     *                 Usually it's method reference (e.g {@code HashMap::new})
     * @param <M> type of resulting map
     * @return map of type {@link M} of reference frequencies
     */
    public <M extends Map<T, Integer>> M referenceCounter(Supplier<? extends M> supplier) {
        return toMap(t -> t, this::referenceFrequency, asRequiringEmpty(supplier));
    }

    /**
     * Creates a map of frequencies using equals comparison based on {@link LinkedHashMap}
     * that has keys as values of this array and values as frequency counts
     * of corresponding values
     * @param supplier supplier that supplies empty map of subtype of {@link Map}.
     *                 Usually it's method reference (e.g {@code HashMap::new})
     * @param <M> type of resulting map
     * @return map of type {@link M} of frequencies
     */
    public <M extends Map<T, Integer>> M counter(Supplier<? extends M> supplier) {
        return toMap(t -> t, this::frequency, asRequiringEmpty(supplier));
    }

    /**
     * Creates an array of strings of lines of specified {@code sequence}
     * <p>
     *     Semantics are the same as {@linkplain String#lines()}
     * </p>
     * @param sequence sequence of characters
     * @return an {@link DelegatingArray} of strings of lines
     */
    public static DelegatingArray<String> ofLines(CharSequence sequence) {
        return sequence.toString().lines().collect(Collectors.toCollection(DelegatingArray::new));
    }

    /**
     * Creates an array of characters of digits of specified {@code value}
     * @param value number
     * @return an {@link DelegatingArray} of characters of digits of specified {@code value}
     * @see BigDecimal
     */
    public static DelegatingArray<Character> ofDigits(double value) {
        return ofCharacters(new BigDecimal(value).toPlainString());
    }

    /**
     * Creates an array of characters of digits of specified {@code value}
     * @param value number
     * @return an {@link DelegatingArray} of characters of digits of specified {@code value}
     */
    public static DelegatingArray<Character> ofDigits(int value) {
        return ofCharacters(String.valueOf(value));
    }

    /**
     * Creates an array of characters of digits of specified {@code value}
     * @param value number
     * @return an {@link DelegatingArray} of characters of digits of specified {@code value}
     */
    public static DelegatingArray<Character> ofDigits(long value) {
        return ofCharacters(String.valueOf(value));
    }

    /**
     * Creates an array of characters of specified {@code sequence}
     * @param sequence sequence of characters
     * @return an {@link DelegatingArray} of characters of specified {@code sequence}
     */
    public static DelegatingArray<Character> ofCharacters(CharSequence sequence) {
        return sequence.chars().mapToObj(i -> (char) i).collect(Collectors.toCollection(DelegatingArray::new));
    }

    /**
     * Transforms this array into {@link LinkedHashMap}
     * @param keyExtractor key extractor
     * @param valueExtractor value extractor
     * @return transformed {@link LinkedHashMap}
     * @param <K> type of keys of resulting map
     * @param <V> type of values of resulting map
     */
    public <K, V> Map<K, V> toMap(Function<? super T, ? extends K> keyExtractor,
                                  Function<? super T, ? extends V> valueExtractor) {
        return toMap(keyExtractor, valueExtractor, LinkedHashMap::new);
    }

    /**
     * Transforms this array into map of type {@link M} with specified extractors
     * @param keyExtractor key extractor
     * @param valueExtractor value extractor
     * @param supplier supplier that supplies empty map of subtype of {@link Map}.
     *                 Usually it's method reference (e.g {@code HashMap::new})
     * @return transformed map
     * @param <K> type of keys of resulting map
     * @param <V> type of values of resulting map
     * @param <M> type of resulting map
     */
    public <K, V, M extends Map<K, V>> M toMap(Function<? super T, ? extends K> keyExtractor,
                                               Function<? super T, ? extends V> valueExtractor,
                                               Supplier<? extends M> supplier) {
        M map = asRequiringEmpty(supplier).get();
        forEach(t -> {
            K key = keyExtractor.apply(t);
            V value = valueExtractor.apply(t);

            map.put(key, value);
        });

        return map;
    }

    /**
     * Transforms this array into {@link LinkedHashSet}
     * @return {@link LinkedHashSet} with elements of this array
     */
    public Set<T> toSet() {
        return copy(LinkedHashSet::new);
    }

    /**
     * Ensures this array is modifiable. Unwraps from unmodifiable wrapper if needed
     * @return modifiable list of this array
     */
    public List<T> asModifiable() {
        return asModifiableArray();
    }

    /**
     * Ensures this array is modifiable. Unwraps from unmodifiable wrapper if needed
     * @return modifiable array of this array
     */
    public DelegatingArray<T> asModifiableArray() {
        if (isModifiable()) {
            return this;
        }

        return copy();
    }

    /**
     * Determines if this array backed up by modifiable implementation.
     * <br>
     * More formally, determines if this array doesn't throw {@link UnsupportedOperationException}
     * on such modifying operations like {@linkplain List#add(Object)} or {@linkplain List#set(int, Object)}
     * @return {@code true} if this array is modifiable, {@code false} otherwise
     */
    @SuppressWarnings("ALL")
    public boolean isModifiable() {
        try {
            set(Integer.MAX_VALUE, get(Integer.MAX_VALUE));
        } catch (UnsupportedOperationException e) {
            return false;
        } catch (Exception e) {
            // Integer.MAX_VALUE in set throws IllegalArgumentException
            return true;
        }

        return true;
    }

    /**
     * Wraps this array with unmodifiable wrapper
     * @return unmodifiable wrapper of this array
     */
    public List<T> asUnmodifiable() {
        return asUnmodifiableArray();
    }

    /**
     * Wraps this array with unmodifiable wrapper
     * @return unmodifiable wrapper of this array
     */
    public DelegatingArray<T> asUnmodifiableArray() {
        if (!isModifiable()) {
            return this;
        }

        return new DelegatingArray<>(Collections.unmodifiableList(this));
    }

    private static <U> EmptyCollectionConstructorRef<U> asRequiringEmpty(Supplier<U> supplier) {
        return EmptyCollectionConstructorRef.of(supplier);
    }

    /**
     * Creates shallow copy of specified type {@code R}
     * @param supplier supplier that supplies empty list of subtype of {@link List}.
     *                 Usually it's method reference (e.g {@code ArrayList::new})
     * @return shallow copy of specified type {@code R}
     * @param <R> type of shallow copy
     */
    public <R extends Collection<T>> R copy(Supplier<? extends R> supplier) {
        R collection = asRequiringEmpty(supplier).get();
        collection.addAll(this);
        return collection;
    }

    /**
     * Rotates the elements in this array by the specified {@code distance}.
     * After calling this method, the element at index {@code i} will be
     * the element previously at index {@code (i - distance)} mod
     * {@code list.size()}, for all values of {@code i} between {@code 0}
     * and {@code list.size()-1}, inclusive.  (This method has no effect on
     * the size of the list.)
     *
     * <p>For example, suppose this array comprises{@code  [t, a, n, k, s]}.
     * After invoking {@code rotate(1)} (or
     * {@code rotate(-4)}), this array will comprise
     * {@code [s, t, a, n, k]}.
     * </p>
     *
     * <p>
     *     Semantics are the same as {@linkplain Collections#rotate(List, int)}
     * </p>
     *
     * @param distance the distance to rotate the list.  There are no
     *        constraints on this value; it may be zero, negative, or
     *        greater than {@code size()}.
     * @throws UnsupportedOperationException if this array or
     *         its list-iterator does not support the {@code set} operation.
     */
    public void rotate(int distance) {
        Collections.rotate(this, distance);
    }

    /**
     * Randomly permutes this array using a default source of
     * randomness.  All permutations occur with approximately equal
     * likelihood.
     *
     * <p>The hedge "approximately" is used in the foregoing description because
     * default source of randomness is only approximately an unbiased source
     * of independently chosen bits. If it were a perfect source of randomly
     * chosen bits, then the algorithm would choose permutations with perfect
     * uniformity.
     *
     * <p>This implementation traverses the list backwards, from the last
     * element up to the second, repeatedly swapping a randomly selected element
     * into the "current position".  Elements are randomly selected from the
     * portion of the list that runs from the first element to the current
     * position, inclusive.
     * <p>
     * Semantics are the same as {@linkplain Collections#shuffle(List)}
     * </p>
     *
     * @throws UnsupportedOperationException if this list is unmodifiable or
     *         its list-iterator does not support the {@code set} operation.
     */
    public void shuffle() {
        Collections.shuffle(this);
    }

    /**
     * Randomly permutes this array specified {@code times} using a default source of
     * randomness. All permutations occur with approximately equal
     * likelihood.
     *
     * <p>The hedge "approximately" is used in the foregoing description because
     * default source of randomness is only approximately an unbiased source
     * of independently chosen bits. If it were a perfect source of randomly
     * chosen bits, then the algorithm would choose permutations with perfect
     * uniformity.
     *
     * <p>This implementation traverses the list backwards, from the last
     * element up to the second, repeatedly swapping a randomly selected element
     * into the "current position".  Elements are randomly selected from the
     * portion of the list that runs from the first element to the current
     * position, inclusive.
     * <p>
     * Semantics are the same as {@linkplain Collections#shuffle(List)}
     * </p>
     *
     * @param times times to shuffle
     * @throws UnsupportedOperationException if this list is unmodifiable or
     *         its list-iterator does not support the {@code set} operation.
     */
    public void shuffle(int times) {
        Runnables.repeatable(this::shuffle, times).run();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    /**
     * Returns an array containing all the elements in this list
     * in proper sequence (from first to last element).
     *
     * @param arraySupplier array constructor reference
     * @return array of elements of this array
     */
    public T[] asArray(IntFunction<T[]> arraySupplier) {
        T[] result = arraySupplier.apply(size());

        int i = 0;
        for (T element : this) {
            result[i] = element;
            i++;
        }

        return result;
    }

    /**
     * Returns a view of the portion of this array between the specified
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.  (If
     * {@code fromIndex} and {@code toIndex} are equal, the returned array is
     * empty.)  The returned array is backed by this array, so non-structural
     * changes in the returned array are reflected in this array, and vice-versa.
     * The returned array supports all the optional array operations supported
     * by this array.<p>
     *
     * This method eliminates the need for explicit range operations (of
     * the sort that commonly exist for arrays).  Any operation that expects
     * an array can be used as a range operation by passing a subArray view
     * instead of a whole array.  For example, the following idiom
     * removes a range of elements from an array:
     * <pre>{@code
     *      array.subArray(from, to).clear();
     * }</pre>
     * Similar idioms may be constructed for {@code indexOf} and
     * {@code lastIndexOf}, and all the algorithms in the
     * {@code Collections} class can be applied to a subarray.<p>
     *
     * The semantics of the array returned by this method become undefined if
     * the backing array (i.e., this array) is <i>structurally modified</i> in
     * any way other than via the returned array.  (Structural modifications are
     * those that change the size of this array, or otherwise perturb it in such
     * a fashion that iterations in progress may yield incorrect results.)
     *
     * @param fromIndex low endpoint (inclusive) of the subArray
     * @param toIndex high endpoint (exclusive) of the subArray
     * @return a view of the specified range within this array
     * @throws IndexOutOfBoundsException for an illegal endpoint index value
     *         ({@code fromIndex < 0 || toIndex > size ||
     *         fromIndex > toIndex})
     */
    public DelegatingArray<T> subArray(int fromIndex, int toIndex) {
        return new DelegatingArray<>(subList(fromIndex, toIndex));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return list.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return list.toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("ALL")
    public <R> @NotNull R[] toArray(@NotNull R[] a) {
        return list.toArray(a);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(T element) {
        return list.add(element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return new HashSet<>(list).containsAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        return list.addAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        return list.addAll(index, c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return list.removeAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return list.retainAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        list.replaceAll(operator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        list.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sort(Comparator<? super T> c) {
        list.sort(c);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public ListIterator<T> listIterator(int index) {
        return list.listIterator(index);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        return list.listIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T remove(int index) {
        return list.remove(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(int index, T element) {
        list.add(index, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T set(int index, T element) {
        return list.set(index, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T get(int index) {
        return list.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return list.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("ALL")
    @Override
    public boolean equals(Object o) {
        return o == this || list.equals(o);
    }

    @Override
    public String toString() {
        return list.toString();
    }
}
