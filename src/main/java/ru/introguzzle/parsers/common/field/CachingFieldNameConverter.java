package ru.introguzzle.parsers.common.field;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.convert.NameConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public abstract class CachingFieldNameConverter<A extends Annotation>
        extends FieldNameConverter<A> {
    public CachingFieldNameConverter(NameConverter converter) {
        super(converter);
    }

    public abstract @NotNull Cache<Field, A> getCache();

    @Override
    public A retrieveAnnotation(Field field) {
        return getCache().get(field, super::retrieveAnnotation);
    }
}
