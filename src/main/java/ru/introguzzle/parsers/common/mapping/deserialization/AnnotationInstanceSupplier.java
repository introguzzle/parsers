package ru.introguzzle.parsers.common.mapping.deserialization;

import lombok.RequiredArgsConstructor;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.common.mapping.AnnotationData;
import ru.introguzzle.parsers.common.mapping.WritingMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Basic skeleton for implementation classes
 *
 * @param <T> the type of the source object used during instance acquisition (e.g., {@code XMLDocument})
 * @param <E> the type of the entity-level annotation providing metadata for constructor argument mapping
 * @param <F> the type of the field-level annotation
 */
@RequiredArgsConstructor
public abstract class AnnotationInstanceSupplier<T, E extends Annotation, F extends Annotation>
        implements InstanceSupplier<T> {
    /**
     * Parent mapper that uses this supplier
     */
    protected final WritingMapper<?> mapper;

    /**
     * Annotation data that provides actual information about {@code E} and {@code F}
     */
    protected final AnnotationData<E, F> annotationData;

    @SuppressWarnings("unchecked")
    public FieldNameConverter<F> getFieldNameConverter() {
        return (FieldNameConverter<F>) mapper.getNameConverter();
    }

    /**
     * Retrieves array of {@code ConstructorArgument} from entity-level annotation
     * @param annotation entity-level annotation
     * @return array of {@code ConstructorArgument}
     */
    public abstract ConstructorArgument[] retrieveConstructorArguments(E annotation);

    /**
     * Retrieves value from {@code object} by name
     * @param object object
     * @param name name
     * @return value from {@code object}
     */
    public abstract Object retrieveValue(T object, String name);

    @SuppressWarnings("unchecked")
    protected <R> Class<R> raw(Type type) {
        return (Class<R>) mapper.getTypeResolver().getRawType(type);
    }
}
