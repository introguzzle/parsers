package ru.introguzzle.parsers.xml.mapping;

import ru.introguzzle.parsers.common.convert.NameConverter;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.xml.entity.annotation.XMLValue;

public class XMLValueNameConverter extends FieldNameConverter<XMLValue> {
    public XMLValueNameConverter(NameConverter converter) {
        super(converter);
    }

    @Override
    public Class<XMLValue> getAnnotationType() {
        return XMLValue.class;
    }

    @Override
    public String retrieveDefaultValue(XMLValue annotation) {
        return annotation.value();
    }
}
