package ru.introguzzle.parser.xml.entity.annotation;

import org.intellij.lang.annotations.MagicConstant;
import ru.introguzzle.parser.common.AccessLevel;
import ru.introguzzle.parser.xml.meta.Encoding;
import ru.introguzzle.parser.xml.meta.Version;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XMLEntity {
    @MagicConstant(valuesFromClass = AccessLevel.class)
    int accessLevel() default AccessLevel.DEFAULT;
    String[] excluded() default {};
    String[] constructorArgs() default {};
    Encoding encoding() default Encoding.UTF_8;
    Version version() default Version.V1_0;
}
