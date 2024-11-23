package ru.introguzzle.parsers.common.mapping;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.json.entity.annotation.JSONEntity;
import ru.introguzzle.parsers.json.entity.annotation.JSONField;
import ru.introguzzle.parsers.xml.entity.annotation.XMLEntity;
import ru.introguzzle.parsers.xml.entity.annotation.XMLField;

import java.lang.annotation.Annotation;

/**
 * A data record that encapsulates metadata about annotations used in mapping processes.
 * This record holds references to the entity-level and field-level annotation classes.
 *
 * @param <E> the type of the entity-level annotation
 * @param <F> the type of the field-level annotation
 * @param entityAnnotationClass the {@code Class} object representing the entity-level annotation
 * @param fieldAnnotationClass the {@code Class} object representing the field-level annotation
 */
public record AnnotationData<E extends Annotation, F extends Annotation>(
        @NotNull Class<E> entityAnnotationClass,
        @NotNull Class<F> fieldAnnotationClass
) {
    public static final AnnotationData<JSONEntity, JSONField> JSON;
    public static final AnnotationData<XMLEntity, XMLField> XML;

    static {
        JSON = new AnnotationData<>(JSONEntity.class, JSONField.class);
        XML = new AnnotationData<>(XMLEntity.class, XMLField.class);
    }

    /**
     * Retrieves entity level annotation class
     * @return entity level annotation class
     */
    @Override
    public Class<E> entityAnnotationClass() {
        return entityAnnotationClass;
    }

    /**
     * Retrieves field level annotation class
     * @return field level annotation class
     */
    @Override
    public Class<F> fieldAnnotationClass() {
        return fieldAnnotationClass;
    }
}
