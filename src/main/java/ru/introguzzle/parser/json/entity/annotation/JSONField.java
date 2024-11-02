package ru.introguzzle.parser.json.entity.annotation;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parser.json.mapping.type.JSONType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JSONField {
    @NotNull String name() default "";
    @NotNull JSONType type() default JSONType.UNSPECIFIED;
}
