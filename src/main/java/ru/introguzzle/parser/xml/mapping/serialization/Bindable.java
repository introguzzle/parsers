package ru.introguzzle.parser.xml.mapping.serialization;

import ru.introguzzle.parser.xml.entity.XMLDocument;

public interface Bindable {
    default XMLDocument toXMLDocument() {
        return null;
    }
}
