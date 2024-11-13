package ru.introguzzle.parsers.xml.mapping.deserialization;

import lombok.AllArgsConstructor;
import lombok.ToString;
import org.junit.Test;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.xml.entity.XMLDocument;
import ru.introguzzle.parsers.xml.entity.annotation.XMLEntity;
import ru.introguzzle.parsers.xml.entity.annotation.XMLField;
import ru.introguzzle.parsers.xml.entity.annotation.XMLRoot;
import ru.introguzzle.parsers.xml.entity.type.XMLType;
import ru.introguzzle.parsers.xml.mapping.serialization.XMLMapper;
import ru.introguzzle.parsers.xml.mapping.serialization.XMLMapperImpl;

import java.util.List;

public class ObjectMapperTest {
    private final XMLMapper xmlMapper = new XMLMapperImpl();
    private final ObjectMapper objectMapper = new ObjectMapperImpl();

    @XMLEntity(constructorArguments = {
            @ConstructorArgument("id"),
            @ConstructorArgument("age"),
            @ConstructorArgument("salary"),
            @ConstructorArgument("name"),
            @ConstructorArgument("isActive")
    })
    @AllArgsConstructor
    @ToString
    public static class Person {
        @XMLField(type = XMLType.ATTRIBUTE)
        int id;

        @XMLField(type = XMLType.ELEMENT)
        int age;

        @XMLField(type = XMLType.ELEMENT)
        Integer salary;

        @XMLField(type = XMLType.ELEMENT)
        String name;

        @XMLField(type = XMLType.ELEMENT)
        boolean isActive;
    }

    @Test
    public void test() {
        Person before = new Person(999, 10, 10, "NAME", true);
        XMLDocument document = xmlMapper.toXMLDocument(before);

        Person after = objectMapper.toObject(document, Person.class);
        System.out.println(after);
    }

    @AllArgsConstructor
    @XMLEntity(constructorArguments = {
            @ConstructorArgument("list"),
            @ConstructorArgument("integers"),
            @ConstructorArgument("objects")
    })
    @ToString
    public static class SomeClass1 {
        @XMLField(type = XMLType.ELEMENT)
        List<Boolean> list;

        @XMLField(type = XMLType.ELEMENT)
        List<Integer> integers;

        @XMLField(type = XMLType.ELEMENT)
        List<SomeClass1> objects;
    }

    @Test
    public void test_list() {
        SomeClass1 before = new SomeClass1(
                List.of(true, true, false),
                List.of(1, 22, 333),
                List.of(new SomeClass1(List.of(false), List.of(444), List.of()))
        );
        XMLDocument document = xmlMapper.toXMLDocument(before);
        SomeClass1 after = objectMapper.toObject(document, SomeClass1.class);
        System.out.println(after);
    }

    @AllArgsConstructor
    @XMLEntity(constructorArguments = {
            @ConstructorArgument("doubleArray"),
            @ConstructorArgument("integerArray"),
            @ConstructorArgument("objectArray")
    })
    @ToString
    @XMLRoot("SOME_CLASS_2")
    public static class SomeClass2 {
        @XMLField(type = XMLType.ELEMENT, name = "DOUBLE_ARRAY", element = "DOUBLE")
        Double[] doubleArray;

        @XMLField(type = XMLType.ELEMENT, name = "INTEGER_ARRAY", element = "INTEGER")
        Integer[] integerArray;

        @XMLField(type = XMLType.ELEMENT, name = "OBJECT_ARRAY", element = "OBJECT")
        SomeClass2[] objectArray;
    }

    @Test
    public void test_array() {
        SomeClass2 before = new SomeClass2(
                new Double[]{1111.1, 2222.2},
                new Integer[]{333, 444, 555, 666},
                new SomeClass2[]{
                        new SomeClass2(new Double[]{777.7}, new Integer[]{888}, new SomeClass2[]{})
                }
        );

        XMLDocument document = xmlMapper.toXMLDocument(before);
        System.out.println(document.toXMLString());

        SomeClass2 after = objectMapper.toObject(document, SomeClass2.class);
        System.out.println(after);
    }
}
