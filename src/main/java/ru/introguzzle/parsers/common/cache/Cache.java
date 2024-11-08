package ru.introguzzle.parsers.common.cache;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.function.ThrowingFunction;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

/**
 * A generic caching interface that defines the core operations for storing, retrieving,
 * and managing cached objects. This interface is designed to be thread-safe and efficient,
 * making it suitable for use in concurrent applications.
 *
 * <p>
 * Implementations of this interface should provide mechanisms for:
 * </p>
 * <ul>
 *     <li>Storing key-value pairs in the cache.</li>
 *     <li>Retrieving values based on their keys.</li>
 *     <li>Invalidating specific entries or the entire cache.</li>
 *     <li>Managing cache size and eviction policies.</li>
 * </ul>
 *
 * <p>
 * The {@link CacheBuilder} nested interface facilitates the construction of {@code Cache} instances
 * with customizable configurations.
 * </p>
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
@SuppressWarnings("unused")
public interface Cache<K, V> {

    /**
     * A builder interface for constructing {@code Cache} instances with specific configurations.
     *
     * <p>
     * Implementations of this builder should allow for the customization of cache parameters
     * such as initial capacity, maximum capacity, eviction policies, and other relevant settings.
     * </p>
     *
     * @param <K> the type of keys maintained by the cache being built
     * @param <V> the type of mapped values in the cache being built
     */
    interface CacheBuilder<K, V> {
        /**
         * Builds and returns a {@code Cache} instance based on the configured parameters.
         *
         * @return a new {@code Cache} instance
         */
        Cache<K, V> build();
    }

    /**
     * Retrieves the underlying {@code ConcurrentMap} that backs this cache.
     *
     * <p>
     * This map provides direct access to the cached key-value pairs and can be used for operations
     * that require more control or inspection of the cache contents. It is important to note that
     * modifications to this map should be performed cautiously to maintain cache integrity.
     * </p>
     *
     * @return the underlying {@code ConcurrentMap} backing this cache
     */
    @NotNull ConcurrentMap<K, V> getBackingMap();

    /**
     * Associates the specified value with the specified key in this cache. If the cache previously
     * contained a mapping for the key, the old value is replaced by the specified value.
     *
     * <p>
     * This operation is thread-safe and can be used concurrently by multiple threads without
     * compromising the cache's integrity.
     * </p>
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @throws NullPointerException if the specified key or value is {@code null}
     */
    void put(K key, V value);

    /**
     * Copies all the mappings from the specified map to this cache. The effect of this call is
     * equivalent to that of calling {@link #put(Object, Object)} on this cache once for each mapping
     * from key to value in the specified map.
     *
     * <p>
     * If the cache previously contained mappings for any of the keys, the old values are replaced
     * by the specified values.
     * </p>
     *
     * <p>
     * This operation is thread-safe and can be used concurrently by multiple threads.
     * </p>
     *
     * @param m mappings to be stored in this cache
     * @throws NullPointerException if the specified map is {@code null}, or if any key or value
     *                              in the map is {@code null}
     */
    void putAll(Map<? extends K, ? extends V> m);

    /**
     * Removes the mapping for a single key from this cache if it is present.
     *
     * <p>
     * If the cache contains a mapping for the specified key, it is removed. If the cache does not
     * contain a mapping for the key, no action is taken.
     * </p>
     *
     * <p>
     * This operation is thread-safe and can be used concurrently by multiple threads.
     * </p>
     *
     * @param key key whose mapping is to be removed from the cache
     * @throws NullPointerException if the specified key is {@code null}
     */
    void invalidate(Object key);

    /**
     * Removes the mappings for the specified keys from this cache if present.
     *
     * <p>
     * Each key in the provided {@code Iterable} is processed, and if a mapping exists for that key
     * in the cache, it is removed.
     * </p>
     *
     * <p>
     * This operation is thread-safe and can be used concurrently by multiple threads.
     * </p>
     *
     * @param keys keys whose mappings are to be removed from the cache
     * @throws NullPointerException if the specified {@code Iterable} or any key within it is {@code null}
     */
    void invalidateAll(Iterable<?> keys);

    /**
     * Removes all the mappings from this cache. The cache will be empty after this call returns.
     *
     * <p>
     * This operation is thread-safe and can be used concurrently by multiple threads.
     * </p>
     *
     * <p>
     * Use this method with caution, as it will clear all cached data, which may affect application performance
     * if the cache is heavily utilized.
     * </p>
     */
    void invalidateAll();

    /**
     * Returns the value to which the specified key is mapped, or {@code null} if this cache contains
     * no mapping for the key.
     *
     * <p>
     * This operation is thread-safe and can be used concurrently by multiple threads.
     * </p>
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@code null} if no mapping exists
     * @throws NullPointerException if the specified key is {@code null}
     */
    V get(Object key);

    /**
     * Returns an {@code Optional} containing the value to which the specified key is mapped,
     * or an empty {@code Optional} if this cache contains no mapping for the key.
     *
     * <p>
     * This method provides a way to handle cache misses without dealing with {@code null} values directly.
     * </p>
     *
     * <p>
     * This operation is thread-safe and can be used concurrently by multiple threads.
     * </p>
     *
     * @param key the key whose associated value is to be returned as an {@code Optional}
     * @return an {@code Optional} containing the value to which the specified key is mapped,
     *         or an empty {@code Optional} if no mapping exists
     * @throws NullPointerException if the specified key is {@code null}
     */
    default Optional<V> getOptional(Object key) {
        return Optional.ofNullable(get(key));
    }

    /**
     * Returns the value to which the specified key is mapped, computing it using the provided mapping
     * function if it is not already present.
     *
     * <p>
     * If the key is not already associated with a value, the {@code mappingFunction} is invoked with
     * the key as its argument and its result is inserted into the cache.
     * </p>
     *
     * <p>
     * This method is thread-safe and ensures that the mapping function is applied at most once per key.
     * </p>
     *
     * @param key             key with which the specified value is to be associated
     * @param mappingFunction the function to compute a value
     * @return the current (existing or computed) value associated with the specified key
     * @throws NullPointerException if the specified key or mapping function is {@code null}
     * @throws RuntimeException     if the mapping function throws an exception
     */
    V get(K key, ThrowingFunction<? super K, ? extends V> mappingFunction);

    /**
     * Returns the number of key-value mappings in this cache.
     *
     * <p>
     * This method provides a snapshot of the cache size at the moment of invocation and may not reflect
     * concurrent modifications.
     * </p>
     *
     * <p>
     * This operation is thread-safe and can be used concurrently by multiple threads.
     * </p>
     *
     * @return the number of key-value mappings in this cache
     */
    int size();

    /**
     * Closes this cache and releases any resources associated with it.
     *
     * <p>
     * After invoking this method, the cache should not be used for further operations.
     * Implementations may perform cleanup tasks such as shutting down background threads or
     * persisting cache data.
     * </p>
     *
     * <p>
     * This operation is idempotent; invoking it multiple times has no additional effect.
     * </p>
     *
     * @throws RuntimeException if an error occurs while closing the cache
     */
    void close();
}
