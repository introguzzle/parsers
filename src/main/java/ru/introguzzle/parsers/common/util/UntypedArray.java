package ru.introguzzle.parsers.common.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public abstract class UntypedArray extends DelegatingArray<Object> {
    public UntypedArray() {
        super();
    }

    public UntypedArray(@NotNull Collection<?> collection) {
        super(collection);
    }

    public UntypedArray(@NotNull Object[] array) {
        super(array);
    }

    public UntypedArray(@NotNull List<?> l) {
        super(l);
    }

    /**
     * Retrieves the value at the specified index and casts it to the desired type.
     *
     * @param index the index of the value to retrieve
     * @param type  the class type to cast to
     * @param <T>   the type to be returned
     * @return the value at the specified index cast to the specified type
     */
    public <T> T get(int index, Class<? extends T> type) {
        return type.cast(get(index));
    }

    /**
     * Retrieves the value at the specified index and casts it to the desired type,
     * or returns a default value if the index is out of bounds or the value is null.
     *
     * @param index        the index of the value to retrieve
     * @param type         the class type to cast to
     * @param defaultValue  the default value to return if the index is out of bounds or the value is null
     * @param <T>          the type to be returned
     * @return the value at the specified index cast to the specified type, or the default value
     */
    public <T> T get(int index, Class<? extends T> type, T defaultValue) {
        Object value = get(index);
        if (value == null) {
            return defaultValue;
        }

        return type.cast(value);
    }
}
