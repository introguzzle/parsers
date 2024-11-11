package ru.introguzzle.parsers.common.mapping;

import java.lang.annotation.Annotation;

public record AnnotationData<E extends Annotation, F extends Annotation>(
        Class<E> entityAnnotationClass,
        Class<F> fieldAnnotationClass
) {
}
