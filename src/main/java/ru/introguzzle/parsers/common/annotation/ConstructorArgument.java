package ru.introguzzle.parsers.common.annotation;

import ru.introguzzle.parsers.common.mapping.deserialization.InstanceSupplier;

import java.lang.annotation.*;

/**
 * Annotation that represents argument in constructor
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ConstructorArguments.class)
public @interface ConstructorArgument {
    /**
     *
     * @see InstanceSupplier
     * @return field name
     */
    String value();

    /**
     * @see InstanceSupplier
     * @return type of corresponding argument
     */
    Class<?> type() default void.class;
}
