package ru.introguzzle.parsers.xml.mapping.deserialization;

import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.xml.entity.XMLDocument;

public interface ObjectMapper {
    <T> T toObject(XMLDocument document, Class<T> type);
    <T> T[] toArray(XMLDocument document, Class<T[]> type);
    FieldAccessor getFieldAccessor();
}
