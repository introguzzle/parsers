package ru.introguzzle.parsers.common.field;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.common.convert.NameConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class FieldNameConverter<A extends Annotation> implements NameConverter {
    private final NameConverter converter;

    @Override
    public final String apply(String s) {
        return converter.apply(s);
    }

    public String apply(Field field) {
        return Optional.ofNullable(retrieveAnnotation(field))
                .map(this::retrieveDefaultValue)
                .filter(s -> !s.isEmpty())
                .orElse(apply(field.getName()));
    }

    public @Nullable A retrieveAnnotation(Field field) {
        return field.getAnnotation(getAnnotationType());
    }

    public abstract Class<A> getAnnotationType();
    public abstract String retrieveDefaultValue(A annotation);
}
