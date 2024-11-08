package ru.introguzzle.parsers.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;

public abstract class DelegatingMap<K, V> implements Map<K, V>, Serializable {
    protected final Map<K, V> map;

    protected DelegatingMap() {
        map = new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    protected DelegatingMap(Map<? extends K, ? extends V> m) {
        map = (Map<K, V>) m;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Map<K, V>> getImplementationClass() {
        return (Class<? extends Map<K, V>>) map.getClass();
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

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        return map.put(key, value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return map.values();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof DelegatingMap)) return false;

        return map.equals(o);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    public boolean add(K key, V value) {
        return put(key, value) != null;
    }
}
