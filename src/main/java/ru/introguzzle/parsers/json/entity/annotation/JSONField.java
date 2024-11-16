package ru.introguzzle.parsers.json.entity.annotation;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.json.mapping.type.JSONType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for specifying serialization/deserialization settings of a JSON field.
 * Used to define the name and type of the field in JSON representation.
 * <br>
 * Applied to class fields, it allows setting a custom name and data type
 * to be used when converting an object to and from JSON.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JSONField {
    /**
     * Specifies the name of the field in the JSON representation.
     * If not set, the name of the field itself will be used.
     *
     * @return the name of the field in JSON
     */
    @NotNull String name() default "";

    /**
     * Specifies the field type for JSON serialization.
     * Used to explicitly define the data type during conversion,
     * which can be helpful for fields with generic types.
     * <br>
     * By default, {@link JSONType#UNSPECIFIED} is used, meaning
     * the type will be determined automatically.
     *
     * @return the field type in JSON
     */
    @NotNull JSONType type() default JSONType.UNSPECIFIED;
}
