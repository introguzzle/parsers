package ru.introguzzle.parsers.common.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.common.function.ThrowingFunction;

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
    private final ConcurrentMap<K, V> map;
    private final int maximalCapacity;
    private final @Nullable Runnable onCloseHandler;
    private final ScheduledExecutorService scheduler;

    private final AtomicInteger size = new AtomicInteger(0);

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
        this.scheduler = scheduler;

        this.scheduler.scheduleAtFixedRate(this::invalidateAll, initialDelay, invalidatePeriod, unit);
    }

    @Override
    public @NotNull ConcurrentMap<K, V> getBackingMap() {
        return map;
    }

    @Override
    public void put(K key, V value) {
        if (size.incrementAndGet() > maximalCapacity) {
            invalidateAll();
        }

        map.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (size.addAndGet(m.size()) > maximalCapacity) {
            invalidateAll();
        }

        map.putAll(m);
    }

    @Override
    @SuppressWarnings("ALL")
    public void invalidate(Object key) {
        size.decrementAndGet();
        map.remove(key);
    }

    @Override
    public void invalidateAll(Iterable<?> keys) {
        for (Object key : keys) {
            invalidate(key);
        }
    }

    @Override
    public void invalidateAll() {
        map.clear();
        size.set(0);
    }

    @Override
    @SuppressWarnings("ALL")
    public V get(Object key) {
        return map.get(key);
    }

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

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void close() {
        if (onCloseHandler != null) {
            onCloseHandler.run();
        }

        invalidateAll();
        scheduler.shutdownNow();
    }

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

        public static <K, V> HashCacheBuilder<K, V> newBuilder() {
            return new HashCacheBuilder<>();
        }

        public HashCacheBuilder<K, V> setInitialCapacity(int initialCapacity) {
            this.initialCapacity = initialCapacity;
            return this;
        }

        public HashCacheBuilder<K, V> setLoadFactor(float loadFactor) {
            this.loadFactor = loadFactor;
            return this;
        }

        public HashCacheBuilder<K, V> setConcurrencyLevel(int concurrencyLevel) {
            this.concurrencyLevel = concurrencyLevel;
            return this;
        }

        public HashCacheBuilder<K, V> setMaximalCapacity(int maximalCapacity) {
            this.maximalCapacity = maximalCapacity;
            return this;
        }

        public HashCacheBuilder<K, V> setInitialDelay(long initialDelay) {
            this.initialDelay = initialDelay;
            return this;
        }

        public HashCacheBuilder<K, V> setInvalidatePeriod(long period, TimeUnit unit) {
            this.invalidatePeriod = period;
            this.unit = unit;
            return this;
        }

        public HashCacheBuilder<K, V> setOnCloseHandler(Runnable onCloseHandler) {
            this.onCloseHandler = onCloseHandler;
            return this;
        }

        public HashCacheBuilder<K, V> setScheduler(ScheduledExecutorService scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        @Override
        public Cache<K, V> build() {
            return new HashCache<>(initialCapacity, loadFactor, concurrencyLevel, maximalCapacity, initialDelay, invalidatePeriod, unit, onCloseHandler, scheduler);
        }
    }
}
