package ru.introguzzle.parsers.xml.entity.annotation;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.xml.entity.type.XMLType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface XMLField {
    String name() default "";
    String element() default "";
    @NotNull XMLType type();
}
