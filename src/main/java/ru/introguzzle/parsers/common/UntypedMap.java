package ru.introguzzle.parsers.common;

import java.util.Map;

public abstract class UntypedMap extends DelegatingMap<String, Object> {
    protected UntypedMap() {
        super();
    }

    protected UntypedMap(Map<? extends String, ?> m) {
        super(m);
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
    public <T> T get(Object key, Class<? extends T> type, T defaultValue) {
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
    public <T> T get(Object key, Class<? extends T> type) {
        return type.cast(get(key));
    }
}
