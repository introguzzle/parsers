package ru.introguzzle.parsers.xml.mapping.deserialization;

import lombok.AllArgsConstructor;
import lombok.ToString;
import org.junit.Test;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.xml.entity.XMLDocument;
import ru.introguzzle.parsers.xml.entity.annotation.XMLEntity;
import ru.introguzzle.parsers.xml.entity.annotation.XMLField;
import ru.introguzzle.parsers.xml.entity.type.XMLType;
import ru.introguzzle.parsers.xml.mapping.serialization.XMLMapper;
import ru.introguzzle.parsers.xml.mapping.serialization.XMLMapperImpl;

import java.util.ArrayList;
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
            @ConstructorArgument("integers")
    })
    @ToString
    public static class L {
        @XMLField(type = XMLType.ELEMENT)
        List<Boolean> list;

        @XMLField(type = XMLType.ELEMENT)
        List<Integer> integers;
    }

    @Test
    public void test_list() {
        L l = new L(List.of(true, true, false), List.of(1, 22, 333));
        XMLDocument document = xmlMapper.toXMLDocument(l);
        L after = objectMapper.toObject(document, L.class);
        System.out.println(after);
    }
}
