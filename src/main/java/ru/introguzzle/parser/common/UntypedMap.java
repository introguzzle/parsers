package ru.introguzzle.parser.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class UntypedMap implements Map<String, Object>, Serializable {
    protected final Map<String, Object> map;

    protected UntypedMap() {
        map = new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    protected UntypedMap(Map<? extends String, ?> m) {
        map = (Map<String, Object>) m;
    }

    @SuppressWarnings("rawtypes")
    public Class<? extends Map> getImplementationClass() {
        return map.getClass();
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
    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    @Override
    public Object get(Object key) {
        return map.get(key);
    }

    @Override
    public Object remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ?> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @NotNull
    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @NotNull
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof UntypedMap)) return false;

        return map.equals(o);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    /**
     * Retrieves the value associated with the specified key, casting it to the desired type,
     * or returns a default value if the key does not exist.
     *
     * @param key         the key for the desired value
     * @param type        the class type to cast to
     * @param defaultValue the default value to return if the key does not exist
     * @param <T>         the type to be returned
     * @return the value associated with the key cast to the specified type, or the default value
     */
    public <T> T get(String key, Class<? extends T> type, T defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }

        return type.cast(value);
    }

    /**
     * Retrieves the value associated with the specified key, casting it to the desired type.
     *
     * @param key  the key for the desired value
     * @param type the class type to cast to
     * @param <T>  the type to be returned
     * @return the value associated with the key cast to the specified type
     */
    public <T> T get(String key, Class<? extends T> type) {
        return type.cast(get(key));
    }

    public boolean add(String key, Object value) {
        return put(key, value) != null;
    }
}
