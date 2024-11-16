package ru.introguzzle.parsers.common.field;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.convert.NameConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * {@inheritDoc}
 */
public abstract class CachingFieldNameConverter<A extends Annotation>
        extends FieldNameConverter<A> {
    public CachingFieldNameConverter(NameConverter converter) {
        super(converter);
    }

    /**
     *
     * @return annotation cache
     */
    public abstract @NotNull Cache<Field, A> getCache();

    /**
     * Retrieves and cache the annotation from the given field.
     *
     * @param field The field from which the annotation is retrieved.
     * @return The annotation of type {@code A}, or {@code null} if the annotation is not present.
     */
    @Override
    public A retrieveAnnotation(Field field) {
        return getCache().get(field, super::retrieveAnnotation);
    }
}
