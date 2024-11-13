package ru.introguzzle.parsers.common.mapping;

import org.jetbrains.annotations.NotNull;

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
) {}
