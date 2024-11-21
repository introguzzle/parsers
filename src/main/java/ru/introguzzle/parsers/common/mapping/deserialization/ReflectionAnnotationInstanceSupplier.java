package ru.introguzzle.parsers.common.mapping.deserialization;

import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.mapping.AnnotationData;
import ru.introguzzle.parsers.common.mapping.MappingException;
import ru.introguzzle.parsers.common.mapping.WritingMapper;
import ru.introguzzle.parsers.common.type.Classes;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

/**
 * An abstract base class that provides reflection-based instance creation using annotations.
 * This class extends {@link AnnotationInstanceSupplier} and utilizes reflection to instantiate
 * objects based on constructor arguments specified via annotations.
 *
 * <p>It leverages entity-level and field-level annotations to determine the constructor to use
 * and the values to pass as arguments during instantiation. If no constructor arguments are specified,
 * it attempts to use the default constructor.</p>
 *
 * @param <T> the type of the source object used during instance acquisition (e.g., {@code XMLDocument})
 * @param <E> the type of the entity-level annotation providing metadata for constructor argument mapping
 * @param <F> the type of the field-level annotation
 */
@ExtensionMethod({Classes.class})
abstract class ReflectionAnnotationInstanceSupplier<T, E extends Annotation, F extends Annotation>
        extends AnnotationInstanceSupplier<T, E, F> {

    /**
     * Constructs a new instance of {@code ReflectionAnnotationInstanceSupplier} with the specified mapper and annotation data.
     *
     * @param mapper         the writing mapper used for accessing field and type information
     * @param annotationData the annotation data containing entity-level and field-level annotation classes
     */
    public ReflectionAnnotationInstanceSupplier(WritingMapper<?> mapper, AnnotationData<E, F> annotationData) {
        super(mapper, annotationData);
    }

    /**
     * Retrieves the names of the constructor arguments specified in the entity annotation.
     *
     * @param annotation the entity-level annotation containing constructor argument metadata
     * @return an array of constructor argument names
     */
    private String[] getConstructorNames(E annotation) {
        return Arrays.stream(retrieveConstructorArguments(annotation))
                .map(ConstructorArgument::value)
                .toArray(String[]::new);
    }

    @Override
    public <R> @NotNull R acquire(@NotNull T object, @NotNull Type type) {
        requireNonNull(object, type);
        Class<R> rawType = rawType(type);
        Optional<E> optional = rawType.retrieveAnnotation(annotationData.entityAnnotationClass());
        if (optional.isEmpty() || retrieveConstructorArguments(optional.get()).length == 0) {
            try {
                return rawType.retrieveDefaultConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw MappingException.ofInstantiation(rawType, e);
            }
        }

        return getWithArguments(object, type, optional.get());
    }

    /**
     * Creates an instance of the specified type using constructor arguments specified in the entity annotation.
     * It matches the constructor argument names to the class fields and retrieves the corresponding values.
     *
     * @param object     the source object used during instance acquisition
     * @param type       the class of the object to be instantiated
     * @param annotation the entity-level annotation containing constructor argument metadata
     * @param <R>        the type of the object to be instantiated
     * @return a new instance of the specified type
     * @throws MappingException if instantiation fails due to reflection errors or invalid constructor arguments
     */
    private <R> R getWithArguments(T object, Type type, E annotation) {
        String[] constructorNames = getConstructorNames(annotation);
        Class<R> rawType = rawType(type);
        List<Field> fields = mapper.getFieldAccessor().acquire(rawType);

        Function<Field, Field> matcher = field -> {
            for (String constructorName : constructorNames) {
                if (constructorName.equals(field.getName())) {
                    return field;
                }
            }
            return null;
        };

        Class<?>[] constructorTypes = fields.stream()
                .map(matcher)
                .filter(Objects::nonNull)
                .map(Field::getType)
                .toArray(Class<?>[]::new);

        if (constructorTypes.length != constructorNames.length) {
            throw new MappingException("Invalid specified constructor arguments: " + Arrays.toString(constructorNames));
        }

        List<Field> matchedFields = fields.stream()
                .map(matcher)
                .toList();

        if (matchedFields.size() != constructorNames.length) {
            throw new MappingException("Invalid specified constructor arguments: " + Arrays.toString(constructorNames));
        }

        Map<String, Type> resolved = mapper.getTypeResolver().resolveTypes(rawType, type);
        Object[] args = matchedFields.stream()
                .map(field -> {
                    String transformed = getFieldNameConverter().apply(field);
                    Type fieldType = resolved.get(field.getName());

                    return mapper.getForwardCaller().apply(retrieveValue(object, transformed), fieldType);
                })
                .toArray();

        try {
            return rawType.getConstructor(constructorTypes).newInstance(args);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new MappingException("Can't instantiate: " + type, e);
        }
    }
}
