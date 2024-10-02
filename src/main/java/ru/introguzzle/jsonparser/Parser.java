package ru.introguzzle.jsonparser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Parser {
    /**
     *
     * @param data - JSON data
     * @return
     * {@code Integer, Double, String} or {@code Boolean} - if it's primitive
     * <br>
     * {@code Map<String, Object>} - if it's non-primitive
     */
    <T> T parse(@Nullable String data, @NotNull Class<? extends T> cls);
}
