package ru.introguzzle.parser.common;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.LambdaMetafactory;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.UnaryOperator;

public abstract class UntypedArray implements List<Object> {
    private final List<Object> list;

    public UntypedArray() {
        list = new ArrayList<>();
    }

    public UntypedArray(@NotNull Collection<?> collection) {
        list = new ArrayList<>(collection);
    }

    public UntypedArray(@NotNull Object[] array) {
        list = new ArrayList<>(List.of(array));
    }

    @SuppressWarnings("unchecked")
    public UntypedArray(@NotNull List<?> l) {
        list = (List<Object>) l;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends List<?>> getImplementationClass() {
        return (Class<? extends List<?>>) list.getClass();
    }

    @NotNull
    @Override
    public List<Object> subList(int fromIndex, int toIndex) {
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
    public Iterator<Object> iterator() {
        return list.iterator();
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return list.toArray();
    }

    @NotNull
    @Override
    public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(Object element) {
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
    public boolean addAll(@NotNull Collection<?> c) {
        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<?> c) {
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
    public void replaceAll(UnaryOperator<Object> operator) {
        list.replaceAll(operator);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public void sort(Comparator<? super Object> c) {
        list.sort(c);
    }

    @NotNull
    @Override
    public ListIterator<Object> listIterator(int index) {
        return list.listIterator(index);
    }

    @NotNull
    @Override
    public ListIterator<Object> listIterator() {
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
    public Object remove(int index) {
        return list.remove(index);
    }

    @Override
    public void add(int index, Object element) {
        list.add(index, element);
    }

    @Override
    public Object set(int index, Object element) {
        return list.set(index, element);
    }

    @Override
    public Object get(int index) {
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

    /**
     * Retrieves the value at the specified index and casts it to the desired type.
     *
     * @param index the index of the value to retrieve
     * @param type  the class type to cast to
     * @param <T>   the type to be returned
     * @return the value at the specified index cast to the specified type
     */
    public <T> T get(int index, Class<? extends T> type) {
        return type.cast(get(index));
    }

    /**
     * Retrieves the value at the specified index and casts it to the desired type,
     * or returns a default value if the index is out of bounds or the value is null.
     *
     * @param index        the index of the value to retrieve
     * @param type         the class type to cast to
     * @param defaultValue  the default value to return if the index is out of bounds or the value is null
     * @param <T>          the type to be returned
     * @return the value at the specified index cast to the specified type, or the default value
     */
    public <T> T get(int index, Class<? extends T> type, T defaultValue) {
        Object value = get(index);
        if (value == null) {
            return defaultValue;
        }

        return type.cast(value);
    }
}
