package ru.introguzzle.parsers.xml.mapping.serialization;

import ru.introguzzle.parsers.xml.entity.XMLDocument;

/**
 * An interface representing an object that can be bound to a {@link XMLMapper} for serialization.
 * It provides a default implementation of the {@linkplain XMLMapper#toXMLDocument(Object) XMLMapper::toXMLDocument} method,
 * which returns {@code null} by default.
 *
 * <p>When implementing this interface,
 * inheritor should override the {@linkplain Bindable#toXMLDocument() Bindable::toXMLDocument} method to enable proper
 * serialization by the {@link XMLMapper}.
 * To ensure that the default behavior is utilized and to
 * allow for dynamic method replacement by the mapper, override the method as follows:</p>
 *
 * <pre>{@code
 * @Override
 * public XMLDocument toXMLDocument() {
 *     return Bindable.super.toXMLDocument();
 * }
 * }</pre>
 *
 * <p>This approach allows the XML mapper to inject or replace the implementation of {@linkplain XMLMapper#toXMLDocument(Object) XMLMapper::toXMLDocument} at runtime,
 * facilitating the serialization process without introducing compile-time dependencies on the mapper's internal implementation.</p>
 */
public interface Bindable {
    /**
     * Converts this object to {@link XMLDocument}
     * @return {@link XMLDocument} conversion of this object
     * @see XMLMapper
     */
    default XMLDocument toXMLDocument() {

        // This will be replaced by actual implementation in runtime
        // by calling XMLMapper::bindTo(Class<? extends Bindable>)
        // XMLMapper mapper = ...
        // return mapper.toXMLDocument(this)

        return null;
    }
}
