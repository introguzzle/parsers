package ru.introguzzle.parser.xml.mapping.deserialization;

import ru.introguzzle.parser.xml.entity.XMLDocument;

public interface ObjectMapper {
    <T> T toObject(XMLDocument document, Class<T> type);
    <T> T[] toArray(XMLDocument document, Class<T[]> type);
}
