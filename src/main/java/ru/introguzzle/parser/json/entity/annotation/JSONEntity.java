package ru.introguzzle.parser.json.entity.annotation;

import org.intellij.lang.annotations.MagicConstant;
import ru.introguzzle.parser.common.AccessLevel;

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
     * @return array of excluded field names
     */
    String[] excluded() default {};

    /**
     *
     * @return array of declared constructor field names
     */
    String[] constructorArguments() default {};
}
