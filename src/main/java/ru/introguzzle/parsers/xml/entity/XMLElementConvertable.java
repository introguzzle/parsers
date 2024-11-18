package ru.introguzzle.parsers.xml.entity;

/**
 * An interface defining a method to convert an object into an {@link XMLElement}.
 * <p>
 * Implementing this interface allows an object to provide a representation of itself as an XML element,
 * which can be used in constructing XML documents or serializing objects to XML format.
 * </p>
 * @see XMLElement
 */
public interface XMLElementConvertable {
    /**
     * Converts this object to an {@link XMLElement}.
     *
     * @return an {@link XMLElement} representing the object's data.
     */
    XMLElement toXMLElement();
}
