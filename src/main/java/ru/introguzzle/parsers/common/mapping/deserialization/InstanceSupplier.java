package ru.introguzzle.parsers.common.mapping.deserialization;

public interface InstanceSupplier<T> {
    <R> R get(T object, Class<R> type);
}
