package ru.introguzzle.parsers.common.cache;

/**
 * Interface that defines capability of creating new {@link Cache} instances
 * They're no any guarantees that {@linkplain CacheSupplier#newCache()} creates
 * a new instances every time.
 */
@FunctionalInterface
public interface CacheSupplier {
    /**
     * @param <K> The type of keys maintained by cache.
     * @param <V> The type of mapped values.
     * @return A new {@link Cache} instance configured with initial capacity, maximal capacity, and invalidation period.
     */
    <K, V> Cache<K, V> newCache();
}
