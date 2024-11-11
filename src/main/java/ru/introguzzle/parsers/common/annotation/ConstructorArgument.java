package ru.introguzzle.parsers.common.annotation;

import ru.introguzzle.parsers.common.mapping.deserialization.InstanceSupplier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConstructorArgument {
    /**
     *
     * @see InstanceSupplier
     * @return field name
     */
    String value();
}
