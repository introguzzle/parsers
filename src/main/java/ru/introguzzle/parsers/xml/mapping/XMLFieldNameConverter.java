package ru.introguzzle.parsers.xml.mapping;

import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.common.utility.NamingUtilities;
import ru.introguzzle.parsers.xml.entity.annotation.XMLField;

public class XMLFieldNameConverter extends FieldNameConverter<XMLField> {
    public XMLFieldNameConverter() {
        super(NamingUtilities::toCamelCase);
    }

    @Override
    public Class<XMLField> getAnnotationType() {
        return XMLField.class;
    }

    @Override
    public String retrieveDefaultValue(XMLField annotation) {
        return annotation.name();
    }
}
