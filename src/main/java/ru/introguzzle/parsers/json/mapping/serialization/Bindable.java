package ru.introguzzle.parsers.json.mapping.serialization;

import ru.introguzzle.parsers.json.entity.JSONObject;

/**
 * An interface representing an object that can be bound to a JSON object mapper for serialization.
 * It provides a default implementation of the `toJSONObject()` method, which returns `null` by default.
 *
 * <p>When implementing this interface, you should override the `toJSONObject()` method to enable proper
 * serialization by the `ObjectToJSONMapper`. To ensure that the default behavior is utilized and to
 * allow for dynamic method replacement by the mapper, override the method as follows:</p>
 *
 * <pre>{@code
 * @Override
 * public JSONObject toJSONObject() {
 *     return Bindable.super.toJSONObject();
 * }
 * }</pre>
 *
 * <p>This approach allows the JSON mapper to inject or replace the implementation of `toJSONObject()` at runtime,
 * facilitating the serialization process without introducing compile-time dependencies on the mapper's internal implementation.</p>
 *
 * {@linkplain JSONMapper#toJSONObject(Object)}
 */
public interface Bindable {
    /**
     * Converts this object to {@link JSONObject}
     * @return {@link JSONObject} conversion of this object
     * @see JSONMapper
     */
    default JSONObject toJSONObject() {

        // This will be replaced by actual implementation in runtime
        // by calling JSONMapper::bindTo(Class<? extends Bindable>)
        // JSONMapper mapper = ...
        // return mapper.toJSONObject(this)

        return null;
    }
}
