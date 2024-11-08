package ru.introguzzle.parsers.common.cache;

public interface CacheSupplier {
    /**
     * @param <K> The type of keys maintained by this cache.
     * @param <V> The type of mapped values.
     * @return A new Cache instance configured with initial capacity, maximal capacity, and invalidation period.
     */
    <K, V> Cache<K, V> newCache();
}
