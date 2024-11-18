package ru.introguzzle.parsers.xml.entity;

import ru.introguzzle.parsers.xml.meta.Encoding;
import ru.introguzzle.parsers.xml.meta.Version;

/**
 * An interface defining methods to convert an object into an {@link XMLDocument}.
 * <p>
 * Implementing this interface allows an object to provide a representation of itself as an XML document,
 * facilitating serialization to XML format. It includes methods to convert the object with or without
 * additional metadata such as encoding and version information.
 * </p>
 * @see XMLDocument
 * @see Encoding
 * @see Version
 */
public interface XMLDocumentConvertable {
    /**
     * Converts this object to an XML document.
     *
     * @return an {@link XMLDocument} representing the object's data.
     */
    XMLDocument toXMLDocument();

    /**
     * Converts this object to an XML document with metadata.
     * <p>
     * The metadata may include details such as encoding and version information, as specified by
     * the {@link Encoding} and {@link Version} classes.
     * </p>
     *
     * @return an {@link XMLDocument} with metadata representing the object's data.
     * @see Encoding
     * @see Version
     */
    XMLDocument toXMLDocumentWithMetadata();
}
