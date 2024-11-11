package ru.introguzzle.parsers.xml.mapping;

import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.common.util.NamingUtilities;
import ru.introguzzle.parsers.xml.entity.annotation.XMLField;
import ru.introguzzle.parsers.xml.entity.annotation.XMLValue;

import java.lang.reflect.Field;
import java.util.Optional;

public class XMLFieldNameConverter extends FieldNameConverter<XMLField> {
    private final XMLValueNameConverter valueConverter;

    public XMLFieldNameConverter() {
        super(NamingUtilities::toCamelCase);
        valueConverter = new XMLValueNameConverter(this);
    }

    @Override
    public String apply(Field field) {
        XMLValue annotation = field.getAnnotation(XMLValue.class);

        return annotation == null
                ? super.apply(field)
                : valueConverter.apply(field);
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
