package ru.introguzzle.parser.json.mapping;

import ru.introguzzle.parser.common.convert.NameConverter;
import ru.introguzzle.parser.json.entity.annotation.JSONField;

import java.lang.reflect.Field;
import java.util.Optional;

public class ReflectionFieldNameConverter extends FieldNameConverter<Field> {
    public ReflectionFieldNameConverter(NameConverter converter) {
        super(converter);
    }

    @Override
    public String convert(Field field) {
        return Optional.ofNullable(field.getAnnotation(JSONField.class))
                .map(JSONField::name)
                .filter(s -> !s.isEmpty())
                .orElse(apply(field.getName()));
    }
}
