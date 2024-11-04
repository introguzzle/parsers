package ru.introguzzle.parser.xml.entity;

import ru.introguzzle.parser.xml.meta.Encoding;
import ru.introguzzle.parser.xml.meta.Version;

public interface XMLDocumentConvertable {
    /**
     * Converts this object to XML document
     * @return XML document
     */
    XMLDocument toXMLDocument();

    /**
     * Converts this object to XML document with metadata
     * @return XML document with metadata
     * @see Encoding
     * @see Version
     */
    XMLDocument toXMLDocumentWithMetadata();
}
