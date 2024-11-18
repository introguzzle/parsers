package ru.introguzzle.parsers.xml.entity.annotation;

import org.intellij.lang.annotations.MagicConstant;
import ru.introguzzle.parsers.common.mapping.AccessPolicy;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.common.annotation.Excluded;
import ru.introguzzle.parsers.xml.meta.Encoding;
import ru.introguzzle.parsers.xml.meta.Version;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XMLEntity {
    /**
     *
     * @return bit flags of access policy
     */
    @MagicConstant(valuesFromClass = AccessPolicy.class)
    int accessPolicy() default AccessPolicy.DEFAULT;

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

    Encoding encoding() default Encoding.UTF_8;
    Version version() default Version.V1_0;
}
