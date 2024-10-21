package ru.introguzzle.parser.json.convert;

import ru.introguzzle.parser.json.convert.primitive.PrimitiveTypeConverter;
import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.json.entity.JSONArray;

/**
 * A converter that transforms raw JSON strings into appropriate Java types.
 * It converts a JSON string into:
 * <br> - {@code JSONObject} (for JSON objects),
 * <br> - {@code JSONArray} (for JSON arrays),
 * <br> - {@code Number} (for numeric values),
 * <br> - {@code Boolean} (for boolean values),
 * <br> - {@code String} (for string values).
 * <br><br>
 * The conversion logic is based on the structure of the input string:
 * <br> - JSON objects are recognized by their curly braces ({}),
 * <br> - JSON arrays by square brackets ([]),
 * <br> - Primitive values like numbers, booleans, and strings are handled by
 *   a {@link PrimitiveTypeConverter}.
 * <br><br>
 * This class uses the {@link PrimitiveTypeConverter} to convert
 * primitive types (like numbers, booleans, and strings), and custom methods
 * for parsing JSON objects and arrays.
 *
 * @see JSONObject
 * @see JSONArray
 * @see PrimitiveTypeConverter
 */
public interface Converter {
    /**
     * Converts the raw JSON string into the corresponding Java object type.
     *
     * @param data the raw JSON string
     * @param type the expected Java type (JSONObject, JSONArray, Number, Boolean or String)
     * @param <T>  the return type based on the provided class
     * @return the converted object of type {@code T}
     */
    <T> T map(String data, Class<? extends T> type);
}
