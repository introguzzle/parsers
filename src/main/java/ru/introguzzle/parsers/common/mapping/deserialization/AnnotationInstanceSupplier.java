package ru.introguzzle.parsers.common.mapping.deserialization;

import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.common.field.GenericTypeAccessor;
import ru.introguzzle.parsers.common.function.TriFunction;
import ru.introguzzle.parsers.common.mapping.AnnotationData;
import ru.introguzzle.parsers.common.mapping.WritingMapper;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 *
 * @param <T> the type of the source object used during instance acquisition (e.g., {@code XMLDocument})
 * @param <E> the type of the primary annotation providing metadata for constructor argument mapping
 * @param <F> the type of the secondary annotation used for field-level mapping or additional configurations
 */
public abstract class AnnotationInstanceSupplier<T, E extends Annotation, F extends Annotation>
        implements InstanceSupplier<T> {

    /**
     * Annotation data
     */
    protected final AnnotationData<E, F> annotationData;

    /**
     * Field accessor of this supplier for obtaining fields and further analyze and inferring
     */
    protected final FieldAccessor fieldAccessor;

    /**
     * Name converter based on {@code F} annotation
     */
    protected final FieldNameConverter<F> nameConverter;

    /**
     * Reference to method for recursive calls
     * @see WritingMapper
     */
    protected final TriFunction<Object, Class<?>, List<Class<?>>, Object> hook;
    protected final GenericTypeAccessor genericTypeAccessor;

    @SuppressWarnings("unchecked")
    public AnnotationInstanceSupplier(WritingMapper<?> mapper, AnnotationData<E, F> annotationData) {
        this.annotationData = annotationData;
        this.fieldAccessor = mapper.getFieldAccessor();
        this.nameConverter = (FieldNameConverter<F>) mapper.getNameConverter();
        this.hook = mapper.getForwardCaller();
        this.genericTypeAccessor = mapper.getGenericTypeAccessor();
    }

    public abstract ConstructorArgument[] retrieveConstructorArguments(E annotation);
    public abstract Object retrieveValue(T object, String name);
}
