package ru.introguzzle.parsers.common.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ru.introguzzle.parsers.common.function.ThrowingFunction;
import ru.introguzzle.parsers.common.util.Nullability;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of {@link Cache} based on {@link ConcurrentHashMap}
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
class HashCache<K, V> implements Cache<K, V> {
    /**
     * Map that backs up this cache
     */
    private final ConcurrentMap<K, V> map;

    /**
     * Maximal capacity. After exceeding this cache will be purged
     */
    private final int maximalCapacity;

    /**
     * Action that will be performed before closing. May be null
     */
    private final @Nullable Runnable onCloseHandler;

    /**
     * Scheduler that controls this cache
     */
    private final ScheduledExecutorService scheduler;

    /**
     * Approximate size. Main purpose of this atomic field is getting rid of calling
     * {@linkplain HashCache#size()} method that can be heavy due to {@link java.util.HashMap}
     * implementation
     */
    private final AtomicInteger size = new AtomicInteger(0);

    /**
     *
     * Constructs new {@link HashCache} instance with specified parameters and scheduler
     * that is started immediately when this cache is created
     *
     * @param initialCapacity initial capacity of underlying {@link java.util.HashMap}
     * @param loadFactor load factor of underlying {@link java.util.HashMap}
     * @param concurrencyLevel concurrency level of underlying {@link java.util.HashMap}
     * @param maximalCapacity maximal capacity
     * @param initialDelay initial delay of {@code scheduler}
     * @param invalidatePeriod trigger period of {@code scheduler}
     * @param unit time unit {@code initialDelay} and {@code invalidatePeriod}
     * @param onCloseHandler handler that executes before closing this cache
     * @param scheduler scheduler that controls this cache
     * @throws NullPointerException if {@code scheduler} is {@code null}
     */
    public HashCache(int initialCapacity,
                     float loadFactor,
                     int concurrencyLevel,
                     int maximalCapacity,
                     long initialDelay,
                     long invalidatePeriod,
                     TimeUnit unit,
                     @Nullable Runnable onCloseHandler,
                     ScheduledExecutorService scheduler) {
        this.map = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
        this.maximalCapacity = maximalCapacity;
        this.onCloseHandler = onCloseHandler;
        this.scheduler = Nullability.requireNonNull(scheduler, "scheduler");

        this.scheduler.scheduleAtFixedRate(this::invalidateAll, initialDelay, invalidatePeriod, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull ConcurrentMap<K, V> getBackingMap() {
        return map;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(K key, V value) {
        if (size.incrementAndGet() > maximalCapacity) {
            invalidateAll();
        }

        map.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (size.addAndGet(m.size()) > maximalCapacity) {
            invalidateAll();
        }

        map.putAll(m);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("ALL")
    public void invalidate(Object key) {
        size.decrementAndGet();
        map.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invalidateAll(Iterable<?> keys) {
        for (Object key : keys) {
            invalidate(key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invalidateAll() {
        map.clear();
        size.set(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("ALL")
    public V get(Object key) {
        return map.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V get(K key, ThrowingFunction<? super K, ? extends V> function) {
        return map.computeIfAbsent(key, k -> {
            V value;

            try {
                value = function.apply(k);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }

            size.incrementAndGet();
            return value;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        if (onCloseHandler != null) {
            onCloseHandler.run();
        }

        invalidateAll();
        scheduler.shutdownNow();
    }

    /**
     * Implementation of {@link CacheBuilder} that builds {@link HashCache}
     * @param <K> the type of keys maintained by this cache
     * @param <V> the type of mapped values
     */
    public static class HashCacheBuilder<K, V> implements CacheBuilder<K, V> {
        private int initialCapacity;
        private float loadFactor;
        private int concurrencyLevel;
        private int maximalCapacity;
        private long initialDelay;
        private long invalidatePeriod;
        private TimeUnit unit;
        private Runnable onCloseHandler;
        private ScheduledExecutorService scheduler;

        /**
         *
         * @param <K> the type of keys maintained by cache
         * @param <V> the type of mapped values
         */
        public static <K, V> HashCacheBuilder<K, V> newBuilder() {
            return new HashCacheBuilder<>();
        }

        /**
         * Sets the initial capacity of the cache.
         *
         * @param initialCapacity the initial number of elements the cache can hold
         * @return this builder
         */
        public HashCacheBuilder<K, V> setInitialCapacity(int initialCapacity) {
            this.initialCapacity = initialCapacity;
            return this;
        }


        /**
         * Sets the load factor of the underlying {@link java.util.HashMap}.
         *
         * @param loadFactor the load factor of the cache
         * @return this builder
         */
        public HashCacheBuilder<K, V> setLoadFactor(float loadFactor) {
            this.loadFactor = loadFactor;
            return this;
        }


        /**
         * Sets the concurrency level of the underlying {@link java.util.concurrent.ConcurrentHashMap}.
         *
         * @param concurrencyLevel the estimated number of concurrently updating threads
         * @return this builder
         */
        public HashCacheBuilder<K, V> setConcurrencyLevel(int concurrencyLevel) {
            this.concurrencyLevel = concurrencyLevel;
            return this;
        }


        /**
         * Sets the maximum capacity of the cache. When this capacity is exceeded, the cache
         * will be purged by invalidating all its contents.
         *
         * @param maximalCapacity the maximum number of elements the cache can hold
         * @return this builder
         */
        public HashCacheBuilder<K, V> setMaximalCapacity(int maximalCapacity) {
            this.maximalCapacity = maximalCapacity;
            return this;
        }


        /**
         * Sets the initial delay before the scheduler starts invalidating cache entries.
         *
         * @param initialDelay the delay before the first invalidation
         * @return this builder
         */
        public HashCacheBuilder<K, V> setInitialDelay(long initialDelay) {
            this.initialDelay = initialDelay;
            return this;
        }

        /**
         * Sets the periodic invalidation interval and its time unit.
         *
         * @param period the period between invalidations
         * @param unit the time unit of the period
         * @return this builder
         */
        public HashCacheBuilder<K, V> setInvalidatePeriod(long period, TimeUnit unit) {
            this.invalidatePeriod = period;
            this.unit = unit;
            return this;
        }

        /**
         * Sets a handler that will be executed when the cache is closed.
         *
         * @param onCloseHandler the handler to be executed before the cache is closed
         * @return this builder
         */
        public HashCacheBuilder<K, V> setOnCloseHandler(Runnable onCloseHandler) {
            this.onCloseHandler = onCloseHandler;
            return this;
        }

        /**
         * Sets scheduler that controls cache that to be built
         * @param scheduler scheduler that controls cache that to built
         * @return this builder
         */
        public HashCacheBuilder<K, V> setScheduler(ScheduledExecutorService scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        /**
         * Builds and returns a {@link HashCache} instance based on the configured parameters.
         *
         * @return a new {@link HashCache} instance
         */
        @Override
        public Cache<K, V> build() {
            return new HashCache<>(initialCapacity, loadFactor, concurrencyLevel, maximalCapacity, initialDelay, invalidatePeriod, unit, onCloseHandler, scheduler);
        }
    }
}
