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
 * Basic skeleton for implementation classes
 *
 * @param <T> the type of the source object used during instance acquisition (e.g., {@code XMLDocument})
 * @param <E> the type of the entity-level annotation providing metadata for constructor argument mapping
 * @param <F> the type of the field-level annotation
 */
public abstract class AnnotationInstanceSupplier<T, E extends Annotation, F extends Annotation>
        implements InstanceSupplier<T> {
    /**
     * Annotation data that provides actual information about {@code E} and {@code F}
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

    /**
     * Accessor that retrieves generic types
     */
    protected final GenericTypeAccessor genericTypeAccessor;

    @SuppressWarnings("unchecked")
    public AnnotationInstanceSupplier(WritingMapper<?> mapper, AnnotationData<E, F> annotationData) {
        this.annotationData = annotationData;
        this.fieldAccessor = mapper.getFieldAccessor();
        this.nameConverter = (FieldNameConverter<F>) mapper.getNameConverter();
        this.hook = mapper.getForwardCaller();
        this.genericTypeAccessor = mapper.getGenericTypeAccessor();
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
}
