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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@ExtensionMethod({Classes.class})
public abstract class ReflectionAnnotationInstanceSupplier<T, E extends Annotation, F extends Annotation>
        extends AnnotationInstanceSupplier<T, E, F> {
    public ReflectionAnnotationInstanceSupplier(WritingMapper<?> mapper, AnnotationData<E, F> annotationData) {
        super(mapper, annotationData);
    }

    private String[] getConstructorNames(E annotation) {
        return Arrays.stream(retrieveConstructorArguments(annotation))
                .map(ConstructorArgument::value)
                .toArray(String[]::new);
    }

    @Override
    public <R> @NotNull R acquire(@NotNull T object, @NotNull Class<R> type) {
        requireNonNull(object, type);
        Optional<E> optional = type.getAnnotationAsOptional(annotationData.entityAnnotationClass());
        if (optional.isEmpty() || retrieveConstructorArguments(optional.get()).length == 0) {
            try {
                return type.getDefaultConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new MappingException(type);
            }
        }

        return getWithArguments(object, type, optional.get());
    }

    private <R> R getWithArguments(T object, Class<R> type, E annotation) {
        String[] constructorNames = getConstructorNames(annotation);
        List<Field> fields = fieldAccessor.acquire(type);

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

        Object[] args = matchedFields.stream()
                .map(field -> {
                    String transformed = nameConverter.apply(field);
                    return hook.apply(retrieveValue(object, transformed), field.getType(), genericTypeAccessor.acquire(field));
                })
                .toArray();

        try {
            return type.getConstructor(constructorTypes).newInstance(args);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new MappingException("Can't instantiate: " + type, e);
        }
    }
}
