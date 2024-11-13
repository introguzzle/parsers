package ru.introguzzle.parsers.common.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;

public abstract class DelegatingArray<T> implements List<T> {
    private final List<T> list;

    public DelegatingArray() {
        list = new ArrayList<>();
    }

    public DelegatingArray(@NotNull Collection<? extends T> collection) {
        list = new ArrayList<>(collection);
    }

    public DelegatingArray(@NotNull T[] array) {
        list = new ArrayList<>(List.of(array));
    }

    @SuppressWarnings("unchecked")
    public DelegatingArray(@NotNull List<? extends T> l) {
        list = (List<T>) l;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends List<?>> getImplementationClass() {
        return (Class<? extends List<?>>) list.getClass();
    }

    @NotNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return list.toArray();
    }

    @NotNull
    @Override
    public <R> R @NotNull [] toArray(@NotNull R @NotNull [] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(T element) {
        return list.add(element);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return new HashSet<>(list).containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        return list.addAll(index, c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        list.replaceAll(operator);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public void sort(Comparator<? super T> c) {
        list.sort(c);
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator(int index) {
        return list.listIterator(index);
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        return list.listIterator();
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public T remove(int index) {
        return list.remove(index);
    }

    @Override
    public void add(int index, T element) {
        list.add(index, element);
    }

    @Override
    public T set(int index, T element) {
        return list.set(index, element);
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || o.getClass() != getClass()) return false;

        return list.equals(o);
    }
}
