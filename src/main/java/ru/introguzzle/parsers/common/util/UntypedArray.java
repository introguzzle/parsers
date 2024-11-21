package ru.introguzzle.parsers.common.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * List that contains elements as {@code Object} instances.
 */
public abstract class UntypedArray extends DelegatingArray<Object> {
    /**
     * Constructs a new empty UntypedArray with {@link ArrayList} as delegate
     */
    public UntypedArray() {
        super();
    }

    /**
     * Constructs a new UntypedArray with {@link ArrayList} as delegate
     * that contains elements of {@code collection}
     *
     * @param collection collection
     */
    public UntypedArray(@NotNull Collection<?> collection) {
        super(collection);
    }

    /**
     * Constructs a new UntypedArray with {@link ArrayList} as delegate
     * that contains elements of {@code array}
     *
     * @param array array
     */
    public UntypedArray(@NotNull Object[] array) {
        super(array);
    }

    /**
     * Constructs a new UntypedArray with specified {@code list} as {@link List} to delegate
     * @param list delegate
     */
    public UntypedArray(@NotNull List<?> list) {
        super(list);
    }

    /**
     * Retrieves the value at the specified index and casts it to {@code type}.
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
     * Retrieves the value at the specified index and casts it to {@code type},
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
