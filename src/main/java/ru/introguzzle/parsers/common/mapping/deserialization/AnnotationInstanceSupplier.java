package ru.introguzzle.parsers.common.mapping.deserialization;

import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.common.mapping.AnnotationData;
import ru.introguzzle.parsers.common.util.ClassExtensions;

import java.lang.annotation.Annotation;
import java.util.function.BiFunction;

@ExtensionMethod(ClassExtensions.class)
@RequiredArgsConstructor
public abstract class AnnotationInstanceSupplier<T, E extends Annotation, F extends Annotation>
        implements InstanceSupplier<T> {
    protected final AnnotationData<E, F> annotationData;
    protected final FieldAccessor fieldAccessor;
    protected final FieldNameConverter<F> nameConverter;
    protected final BiFunction<Object, Class<?>, Object> hook;

    public abstract ConstructorArgument[] retrieveConstructorArguments(E annotation);
    public abstract Object retrieveValue(T object, String name);
}
