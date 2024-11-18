package ru.introguzzle.parsers.common.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class UntypedMap extends DelegatingMap<String, Object> {
    protected UntypedMap() {
        super();
    }

    protected UntypedMap(Map<? extends String, ?> m) {
        super(m);
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException if {@code key} is null
     */
    @Override
    public @Nullable Object put(@NotNull String key, @Nullable Object value) {
        return super.put(Nullability.requireNonNull(key, "key"), value);
    }

    /**
     * Retrieves value associated with specified key, casting it to the desired type,
     * or returns a default value if key does not exist.
     *
     * @param key         key whose associated value is to be returned
     * @param type        class type to cast to
     * @param defaultValue default value to return if key does not exist
     * @param <T>         type to be returned
     * @return value associated with the key cast to specified type, or default value
     * @throws NullPointerException if {@code key} or {@code type} is null
     */
    public <T> @Nullable T get(@NotNull Object key, @NotNull Class<? extends T> type, @Nullable T defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }

        return type.cast(value);
    }

    @Override
    public Object get(Object key) {
        Nullability.requireNonNull(key, "key");
        return super.get(key);
    }

    /**
     * @param key the key whose associated value class is to be returned
     * @return class of value associated with key or {@code null} if value is null
     * @throws NullPointerException if {@code key} is null
     */
    public @Nullable Class<?> getElementClass(@NotNull String key) {
        Object value = get(key);
        return value == null ? null : value.getClass();
    }

    /**
     * Retrieves the value associated with the specified key, casting it to the desired type.
     *
     * @param key  the key for the desired value
     * @param type the class type to cast to
     * @param <T>  the type to be returned
     * @return the value associated with the key cast to the specified type
     * @throws NullPointerException if {@code key} or {@code type} is null
     */
    public <T> T get(@NotNull Object key, @NotNull Class<? extends T> type) {
        Nullability.requireNonNull(type, "type");
        return type.cast(get(key));
    }
}
