package ru.introguzzle.parsers.json.mapping;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import ru.introguzzle.parsers.common.annotation.Excluded;
import ru.introguzzle.parsers.json.entity.annotation.JSONEntity;
import ru.introguzzle.parsers.json.entity.annotation.JSONField;
import ru.introguzzle.parsers.json.mapping.type.JSONType;

import java.util.*;

@UtilityClass
public final class Data {
    @AllArgsConstructor
    @Getter
    public static class Transient {
        private transient String stringString;
        private transient int intInt;
    }

    @AllArgsConstructor
    @Getter
    public static class BaseWithAnnotations {
        @JSONField(type = JSONType.STRING)
        public final String originalString;

        @JSONField(type = JSONType.NUMBER)
        public final byte originalByte;
    }

    @Getter
    public static class InheritingWithAnnotations extends BaseWithAnnotations {
        @JSONField(type = JSONType.NUMBER)
        public final double inheritingDouble;

        public InheritingWithAnnotations(String originalString,
                                         byte originalByte,
                                         double inheritingDouble) {
            super(originalString, originalByte);
            this.inheritingDouble = inheritingDouble;
        }
    }

    @AllArgsConstructor
    @Getter
    public static class Base {
        public final String originalString;
        public final byte originalByte;
    }

    @Getter
    public static class Inheriting extends Base {
        public final double inheritingDouble;

        public Inheriting(String originalString,
                          byte originalByte,
                          double inheritingDouble) {
            super(originalString, originalByte);
            this.inheritingDouble = inheritingDouble;
        }
    }

    @RequiredArgsConstructor
    @Getter
    public static final class Node {
        private final String name;
        private final Integer boxedAge;
        private final int age;
        private final String email;
        private final Node next;
    }

    @lombok.Data
    public static class Person {
        private final int age;
        private final String name;
        private final double salary;
        private final boolean deleted;
    }

    @lombok.Data
    public static class PersonBoxed {
        private final Integer age;
        private final String name;
        private final Double salary;
        private final Boolean deleted;
    }

    @JSONEntity(excluded = {
            @Excluded("object")
    })
    @AllArgsConstructor
    @Getter
    public static class CollectionsData {
        private final int[] intArray;
        private final List<String> stringList;
        private final Set<Gender> enumSet;
        private final Map<String, Direction> stringToDirectionMap;
        private final List<Date> datesOf;
        private final Object object = null;
    }

    public enum Direction {
        LEFT, RIGHT, DOWN, UP
    }

    public enum Gender {
        MALE, FEMALE
    }
}
