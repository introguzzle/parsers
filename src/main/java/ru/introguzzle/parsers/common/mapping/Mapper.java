package ru.introguzzle.parsers.common.mapping;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.field.FieldNameConverter;

import java.lang.annotation.Annotation;

public interface Mapper {

    /**
     * Retrieves the {@link FieldNameConverter} used for converting field names based on annotations.
     *
     * @return a non-null {@link FieldNameConverter} instance parameterized with a subtype of {@link Annotation}
     */
    @NotNull
    FieldNameConverter<? extends Annotation> getNameConverter();

    /**
     * Retrieves the {@link FieldAccessor} responsible for accessing fields of the bound classes.
     *
     * @return a non-null {@link FieldAccessor} instance
     */
    @NotNull
    FieldAccessor getFieldAccessor();

    /**
     * Retrieves the {@link Traverser} used for traversing class hierarchies or structures.
     *
     * @return a non-null {@link Traverser} instance parameterized with {@code Class<?>}
     */
    @NotNull
    Traverser<Class<?>> getTraverser();
}
