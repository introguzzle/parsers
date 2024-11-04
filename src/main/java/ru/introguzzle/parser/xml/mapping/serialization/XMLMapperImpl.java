package ru.introguzzle.parser.xml.mapping.serialization;

import ru.introguzzle.parser.json.mapping.serialization.Bindable;
import ru.introguzzle.parser.xml.entity.XMLDocument;
import ru.introguzzle.parser.xml.entity.XMLDocumentConvertable;
import ru.introguzzle.parser.xml.entity.annotation.XMLEntity;

public class XMLMapperImpl implements XMLMapper {

    @Override
    public XMLDocument toXMLDocument(Object object) {
        if (object == null) {
            return null;
        }

        if (object instanceof XMLDocument document) {
            return document;
        }

        if (object instanceof XMLDocumentConvertable convertable) {
            return convertable.toXMLDocument();
        }

        Class<?> type = object.getClass();
        XMLEntity annotation = type.getAnnotation(XMLEntity.class);
        if (annotation == null) {

        }

        return null;
    }

    @Override
    public XMLMapper bindTo(Class<? extends Bindable> targetType) {
        return null;
    }
}
