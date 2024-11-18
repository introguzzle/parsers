package ru.introguzzle.parsers.json.mapping;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.JSONObject;

/**
 * An interface that defines a contract for objects that can be converted to a {@link JSONObject}.
 * <p>
 * Implementing this interface allows an object to provide a representation of itself as a JSON object,
 * facilitating serialization to JSON format.
 * </p>
 *
 * @see JSONObject
 * @see JSONArray
 */
public interface JSONObjectConvertable {
    /**
     * Converts this object into a {@link JSONObject}.
     *
     * @return a {@link JSONObject} representation of the object.
     */
    @NotNull JSONObject toJSONObject();
}
