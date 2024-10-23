package ru.introguzzle.parser.common;

import org.jetbrains.annotations.NotNull;

/**
 * A generic interface that defines a contract for converting objects from one type to another.
 *
 * @param <T> the input type to be converted
 * @param <R> the output type after conversion
 */
public interface Converter<T, R> {

    /**
     * Converts the given input of type {@code T} to an output of type {@code R} with metadata included
     *
     * @param input the input object to be converted
     * @return the converted object with metadata included of type {@code R}
     */
    @NotNull R convertWithMetadata(@NotNull T input);

    /**
     * Converts the given input of type {@code T} to an output of type {@code R}
     *
     * @param input the input object to be converted
     * @return the converted object of type {@code R}
     */
    @NotNull R convert(@NotNull T input);

    /**
     * Retrieves the {@link NameConverter} instance used for converting names during
     * the conversion process.
     *
     * @return the {@link NameConverter} in use
     */
    @NotNull NameConverter getNameConverter();

    /**
     * Sets the {@link NameConverter} instance to be used for converting names during
     * the conversion process.
     *
     * @param nameConverter the {@link NameConverter} to be set
     */
    void setNameConverter(@NotNull NameConverter nameConverter);
}
