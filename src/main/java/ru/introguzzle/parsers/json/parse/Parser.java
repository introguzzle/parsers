package ru.introguzzle.parsers.json.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ru.introguzzle.parsers.common.parse.BaseParser;
import ru.introguzzle.parsers.common.util.NumberUtilities;
import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.json.parse.tokenize.Tokenizer;

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
 */
public abstract class Parser implements BaseParser<Object> {
    protected Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public final Parser setExecutor(Executor executor) {
        this.executor = executor;
        return this;
    }

    public static Parser newParser() {
        return new JSONParser();
    }

    public static Parser newTokenParser() {
        return new JSONTokenParser();
    }

    public static Parser newTokenParser(Tokenizer tokenizer) {
        return new JSONTokenParser(tokenizer);
    }

    @Override
    public final Object parse(@NotNull String data) {
        return parse(data, Object.class);
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
    public abstract <T> @Nullable T parse(@Nullable String data,
                                          @NotNull Class<? extends T> type);

    public final <T> @NotNull CompletableFuture<T> parseAsync(@Nullable String data,
                                                              @NotNull Class<? extends T> type) {
        Supplier<T> supplier = () -> parse(data, type);
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    final <T> @Nullable T handleEmptyString(@NotNull String data,
                                            @NotNull Class<? extends T> type) {
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

    public Object handlePrimitiveType(@NotNull String data, @NotNull Class<?> type) {
        switch (data.trim()) {
            case "false" -> {
                return Boolean.FALSE;
            }

            case "true" -> {
                return Boolean.TRUE;
            }

            case "null" -> {
                return null;
            }
        }

        String unescaped = unescape(data);

        if (type == Number.class) {
            return Double.parseDouble(unescaped);
        }

        if (data.startsWith("\"") && data.endsWith("\"")) {
            return unescaped;
        }

        if (NumberUtilities.isNumeric(unescaped)) {
            return Double.parseDouble(unescaped);
        }

        throw new JSONParseException("Not a numeric or string value: " + data);
    }

    private static String unescape(@NotNull String data) {
        return data.replace("\"", "");
    }
}
