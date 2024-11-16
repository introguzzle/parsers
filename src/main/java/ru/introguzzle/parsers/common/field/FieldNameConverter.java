package ru.introguzzle.parsers.common.field;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.common.convert.NameConverter;
import ru.introguzzle.parsers.xml.entity.annotation.XMLField;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;

/**
 * An abstract class that provides functionality for converting field names, using a provided {@link NameConverter}.
 * <p>
 * This class is designed to be extended by concrete implementations that provide specific behavior for handling
 * annotations that may define alternative field names or naming strategies.
 * </p>
 *
 * <p>
 * The field name conversion is first attempted using the value from the annotation (if present). If no annotation or
 * value is found, the default behavior is to apply the {@link NameConverter} to the field's name.
 * </p>
 *
 * @param <A> The type of the annotation that holds the custom field name (e.g., {@link XMLField})
 *
 * @see NameConverter
 * @see Field
 */
@RequiredArgsConstructor
public abstract class FieldNameConverter<A extends Annotation> implements NameConverter {

    // The NameConverter used to perform the default name conversion
    private final NameConverter converter;

    /**
     * Converts the given field name using the provided {@link NameConverter}.
     *
     * @param s The string to be converted (typically the name of a field).
     * @return The converted field name.
     */
    @Override
    public final String apply(String s) {
        return converter.apply(s);
    }

    /**
     * Attempts to retrieve a custom field name from the annotation, if present, or falls back to applying the default
     * name conversion strategy to the field's name.
     *
     * @param field The field for which the name conversion is applied.
     * @return The converted field name, either from the annotation or the default name.
     */
    public String apply(Field field) {
        return Optional.ofNullable(retrieveAnnotation(field))
                .map(this::retrieveDefaultValue)
                .filter(s -> !s.isEmpty())
                .orElse(apply(field.getName()));
    }

    /**
     * Retrieves the annotation from the given field.
     *
     * @param field The field from which the annotation is retrieved.
     * @return The annotation of type {@code A}, or {@code null} if the annotation is not present.
     */
    public @Nullable A retrieveAnnotation(Field field) {
        return field.getAnnotation(getAnnotationType());
    }

    /**
     * Gets the class type of the annotation that is used to define the custom field name.
     *
     * @return The annotation type class.
     */
    public abstract Class<A> getAnnotationType();

    /**
     * Retrieves the custom field name value from the annotation.
     *
     * @param annotation The annotation from which the field name is extracted.
     * @return The custom field name defined in the annotation.
     */
    public abstract String retrieveDefaultValue(A annotation);
}
