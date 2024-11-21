package ru.introguzzle.parsers.common.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.UnaryOperator;

/**
 * Class that delegates all functionality to underlying actual implementation of {@link List}
 * @param <T> the type of elements in this list
 */
public abstract class DelegatingArray<T> implements List<T> {
    /**
     * Delegate list that backs up this DelegatingMap
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
     * Retrieves class of actual underlying delegate list
     * @return class of underlying delegate list
     */
    @SuppressWarnings("unchecked")
    public Class<? extends List<T>> getImplementationClass() {
        return (Class<? extends List<T>>) (list instanceof DelegatingArray
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
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
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
}
