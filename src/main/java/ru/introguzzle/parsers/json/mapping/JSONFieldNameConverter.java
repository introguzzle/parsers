package ru.introguzzle.parsers.json.mapping;

import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.common.utility.NamingUtilities;
import ru.introguzzle.parsers.json.entity.annotation.JSONField;

public class JSONFieldNameConverter extends FieldNameConverter<JSONField> {
    public JSONFieldNameConverter() {
        super(NamingUtilities::toSnakeCase);
    }

    @Override
    public Class<JSONField> getAnnotationType() {
        return JSONField.class;
    }

    @Override
    public String retrieveDefaultValue(JSONField annotation) {
        return annotation.name();
    }
}
