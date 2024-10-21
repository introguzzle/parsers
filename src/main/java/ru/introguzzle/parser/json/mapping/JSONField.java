package ru.introguzzle.parser.json.mapping;

import ru.introguzzle.parser.json.mapping.type.JSONType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JSONField {
    String name() default "";
    JSONType type() default JSONType.OBJECT;
}
