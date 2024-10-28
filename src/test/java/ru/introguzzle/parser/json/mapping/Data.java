package ru.introguzzle.parser.json.mapping;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

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
    public static class Base {
        public final String string;
        public final byte b;
    }

    @Getter
    public static class Inheriting extends Base {
        public final double d;

        public Inheriting(String string, byte b, double d) {
            super(string, b);
            this.d = d;
        }
    }

    @RequiredArgsConstructor
    @Getter
    public static final class RecursiveList {
        private final String name;
        private final int age;
        private final String email;
        private final List<RecursiveList> list = new ArrayList<>();
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
}
