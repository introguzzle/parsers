package ru.introguzzle.parsers.common.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;

/**
 * Class that delegates all functionality to underlying actual implementation of {@link Map}
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public abstract class DelegatingMap<K, V> implements Map<K, V>, Serializable {
    /**
     * Delegate map that backs up this DelegatingMap
     */
    protected final Map<K, V> map;

    /**
     * Constructs a new empty DelegatingMap with {@link LinkedHashMap} as delegate
     */
    protected DelegatingMap() {
        map = new LinkedHashMap<>();
    }

    /**
     * Constructs a new DelegatingMap with specified {@code m} as map to delegate
     * @param m delegate
     */
    @SuppressWarnings("unchecked")
    protected DelegatingMap(Map<? extends K, ? extends V> m) {
        map = (Map<K, V>) m;
    }

    /**
     * Retrieves class of actual underlying delegate map
     * @return class of underlying delegate map
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Map<K, V>> getImplementationClass() {
        return (Class<? extends Map<K, V>>) (map instanceof DelegatingMap<?, ?>
                ? ((DelegatingMap<K, V>) map).getImplementationClass()
                : map.getClass());
    }

    /**
     * Returns {@code true} if this map contains a mapping for the specified
     * key.  More formally, returns {@code true} if and only if
     * this map contains a mapping for a key {@code k} such that
     * {@code Objects.equals(key, k)}.  (There can be
     * at most one such mapping.)
     *
     * @param key key whose presence in this map is to be tested
     * @return {@code true} if this map contains a mapping for the specified
     *         key
     *
     **/
    @SuppressWarnings("ALL")
    public boolean has(Object key) {
        return map.containsKey(key);
    }

    /**
     * @inheritDoc
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    /**
     * @inheritDoc
     */
    @Nullable
    @Override
    public V put(K key, V value) {
        return map.put(key, value);
    }

    /**
     * @inheritDoc
     */
    @Override
    public V get(Object key) {
        return map.get(key);
    }

    /**
     * @inheritDoc
     */
    @Override
    public V remove(Object key) {
        return map.remove(key);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void clear() {
        map.clear();
    }

    /**
     * @inheritDoc
     */
    @NotNull
    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    /**
     * @inheritDoc
     */
    @NotNull
    @Override
    public Collection<V> values() {
        return map.values();
    }

    /**
     * @inheritDoc
     */
    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    /**
     * @inheritDoc
     */
    @Override
    @SuppressWarnings("ALL")
    public boolean equals(Object o) {
        return o == this || map.equals(o);
    }

    /**
     * @inheritDoc
     */
    @Override
    public int hashCode() {
        return map.hashCode();
    }

    /**
     * @inheritDoc
     */
    public boolean add(K key, V value) {
        return put(key, value) != null;
    }
}
