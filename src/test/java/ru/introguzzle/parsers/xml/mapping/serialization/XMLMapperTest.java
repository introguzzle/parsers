package ru.introguzzle.parsers.xml.mapping.serialization;

import lombok.AllArgsConstructor;
import org.junit.Test;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.xml.entity.XMLDocument;
import ru.introguzzle.parsers.xml.entity.XMLElement;
import ru.introguzzle.parsers.xml.entity.annotation.XMLEntity;
import ru.introguzzle.parsers.xml.entity.annotation.XMLField;
import ru.introguzzle.parsers.xml.entity.annotation.XMLRoot;
import ru.introguzzle.parsers.xml.entity.annotation.XMLValue;
import ru.introguzzle.parsers.xml.entity.type.XMLType;

public class XMLMapperTest {
    private final XMLMapper mapper = new XMLMapperImpl();

    @AllArgsConstructor
    @XMLEntity(constructorArguments = {
            @ConstructorArgument("name"),
            @ConstructorArgument("age")
    })
    @XMLRoot("Person")
    static class Person implements Bindable {
        @XMLField(type = XMLType.ATTRIBUTE, name = "attribute")
        double version;

        @XMLField(type = XMLType.ELEMENT, name = "Age")
        int age;

        @XMLValue
        String name;

        @XMLField(type = XMLType.ELEMENT, name = "Salary")
        Salary salary;

        @Override
        public XMLDocument toXMLDocument() {
            return Bindable.super.toXMLDocument();
        }
    }

    @AllArgsConstructor
    @XMLEntity
    static class Salary {
        @XMLValue
        float value;

        @XMLField(type = XMLType.ELEMENT, name = "Type")
        Type type;
    }

    enum Type {
        TYPE_1,
        TYPE_2,
        TYPE_3
    }

    @Test
    public void test() {
        Person person = new Person(1.11, 1337, "NAME", new Salary(9999, Type.TYPE_1));
        System.out.println(mapper.toXMLDocument(person).toXMLString());
    }

    @Test
    public void test_inject() {
        Person person = new Person(1.11, 1337, "NAME", new Salary(9999, Type.TYPE_1));
        mapper.bindTo(Person.class);
        System.out.println(person.toXMLDocument());
    }
}