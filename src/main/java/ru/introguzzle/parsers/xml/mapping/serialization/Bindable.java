package ru.introguzzle.parsers.xml.mapping.serialization;

import ru.introguzzle.parsers.xml.entity.XMLDocument;

public interface Bindable {
    default XMLDocument toXMLDocument() {
        return null;
    }
}
