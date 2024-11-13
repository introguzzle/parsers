package ru.introguzzle.parsers.json.entity.annotation;

import org.intellij.lang.annotations.MagicConstant;
import ru.introguzzle.parsers.common.mapping.AccessLevel;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.common.annotation.Excluded;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JSONEntity {

    /**
     *
     * @return bit flags of access level
     * @see AccessLevel
     */
    @MagicConstant(valuesFromClass = AccessLevel.class)
    int accessLevel() default AccessLevel.DEFAULT;
    /**
     *
     * @return array of excluded annotations
     */
    Excluded[] excluded() default {};

    /**
     *
     * @return array of constructor argument annotations
     */
    ConstructorArgument[] constructorArguments() default {};
}
