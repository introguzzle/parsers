package ru.introguzzle.parser.json.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ru.introguzzle.parser.json.convert.Converter;
import ru.introguzzle.parser.json.entity.JSONArray;
import ru.introguzzle.parser.json.entity.JSONObject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * A converter that transforms raw JSON strings into appropriate Java types.
 * It converts a JSON string into:
 * <br> - {@code JSONObject} (for JSON objects),
 * <br> - {@code JSONArray} (for JSON arrays),
 * <br> - {@code Number} (for numeric values),
 * <br> - {@code Boolean} (for boolean values),
 * <br> - {@code String} (for string values).
 *
 * @see JSONObject
 * @see JSONArray
 * @see Number
 * @see Boolean
 * @see String
 * @see Converter
 */
public abstract class Parser {
    protected Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public final Parser setExecutor(Executor executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Validates and parses the raw JSON string into the corresponding Java object type.
     *
     * @param data the raw JSON string; may be null or empty
     * @param type the expected Java type (JSONObject, JSONArray, String, or other supported types)
     * @param <T>  the return type based on the provided class
     * @return {@code null} - if {@code data} is {@code null} or blank (empty string); <br>
     *         <br> an empty {@code JSONObject} - if {@code data} is an empty string and {@code type} is {@code JSONObject.class}; <br>
     *         <br> an empty {@code JSONArray}  - if {@code data} is an empty string and {@code type} is {@code JSONArray.class}; <br>
     *         <br> otherwise, the converted object of type {@code T} <br>
     */
    public abstract <T> T parse(@Nullable String data,
                                @NotNull Class<? extends T> type);

    public final <T> CompletableFuture<T> parseAsync(@Nullable String data,
                                                     @NotNull Class<? extends T> type) {
        Supplier<T> supplier = () -> parse(data, type);
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    final <T> @Nullable T handleEmptyString(@NotNull String data, Class<? extends T> type) {
        Object result = null;

        if (type == JSONObject.class) {
            result = new JSONObject();
        }

        if (type == JSONArray.class) {
            result = new JSONArray();
        }

        if (type == String.class) {
            result = data;
        }

        return result == null ? null : type.cast(result);
    }
}
