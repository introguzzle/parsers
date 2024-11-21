package ru.introguzzle.parsers.json.mapping.serialization;

import ru.introguzzle.parsers.json.entity.JSONObject;

/**
 * An interface representing an object that can be bound to a {@link JSONMapper} for serialization.
 * It provides a default implementation of the {@linkplain Bindable#toJSONObject() Bindable::toJSONObject} method, which returns {@code null} by default.
 *
 * <p>When implementing this interface, inheritor should override the {@linkplain Bindable#toJSONObject() Bindable::toJSONObject} method to enable proper
 * serialization by the {@link JSONMapper}. To ensure that the default behavior is utilized and to
 * allow for dynamic method replacement by the mapper, override the method as follows:</p>
 *
 * <pre>{@code
 * @Override
 * public JSONObject toJSONObject() {
 *     return Bindable.super.toJSONObject();
 * }
 * }</pre>
 *
 * <p>This approach allows the {@link JSONMapper} to inject or replace the implementation of {@linkplain Bindable#toJSONObject() Bindable::toJSONObject} at runtime,
 * facilitating the serialization process without introducing compile-time dependencies on the mapper's internal implementation.</p>
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
