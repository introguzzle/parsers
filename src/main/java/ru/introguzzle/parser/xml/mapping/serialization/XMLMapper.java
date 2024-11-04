package ru.introguzzle.parser.xml.mapping.serialization;

import ru.introguzzle.parser.json.mapping.serialization.Bindable;
import ru.introguzzle.parser.xml.entity.XMLDocument;

public interface XMLMapper {
    XMLDocument toXMLDocument(Object object);
    XMLMapper bindTo(Class<? extends Bindable> targetType);
}
