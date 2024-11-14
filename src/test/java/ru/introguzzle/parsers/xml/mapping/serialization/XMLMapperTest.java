package ru.introguzzle.parsers.xml.mapping.serialization;

import lombok.AllArgsConstructor;
import org.junit.Test;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.xml.entity.XMLDocument;
import ru.introguzzle.parsers.xml.entity.annotation.XMLEntity;
import ru.introguzzle.parsers.xml.entity.annotation.XMLField;
import ru.introguzzle.parsers.xml.entity.annotation.XMLRoot;
import ru.introguzzle.parsers.xml.entity.annotation.XMLValue;
import ru.introguzzle.parsers.xml.entity.type.XMLType;

import java.util.List;

public class XMLMapperTest {
    private final XMLMapper mapper = XMLMapper.newMapper();

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
        System.out.println(person.toXMLDocument().toXMLString());
    }

    @AllArgsConstructor
    @XMLEntity
    @XMLRoot("Node")
    static class Node {
        @XMLValue
        int intValue;

        @XMLField(type = XMLType.ELEMENT, name = "__renamed__")
        String stringValue;

        @XMLField(type = XMLType.ELEMENT, name = "Node")
        Node next;
    }

    @Test
    public void test_node() {
        Node node = new Node(-1, "-1", new Node(0, "0", new Node(1,  "1", null)));
        XMLDocument document = mapper.toXMLDocument(node);
        System.out.println(document.toXMLString());
    }

    @AllArgsConstructor
    @XMLEntity
    @XMLRoot("company")
    static class Company {
        @XMLField(type = XMLType.ELEMENT, name = "EmployeeList", element = "employee")
        List<Employee> employeeList;

        @XMLField(type = XMLType.ELEMENT, name = "EmployeeArray", element = "employee")
        Employee[] employeeArray;
    }

    @AllArgsConstructor
    @XMLEntity
    @XMLRoot("employee")
    static class Employee {
        @XMLField(type = XMLType.ELEMENT, name = "id")
        int id;

        @XMLField(type = XMLType.ELEMENT, name = "name")
        String name;

        @XMLValue
        Throwable throwable;
    }

    @Test
    public void test_iterable_and_array() {
        List<Employee> employeeList = List.of(
            new Employee(1, "111", new Throwable("1T")),
            new Employee(2, "222", new Throwable("2T")),
            new Employee(3, "333", new Throwable("3T"))
        );

        Employee[] employeeArray = {
                new Employee(4, "444", new Throwable("4T")),
                new Employee(5, "555", new Throwable("5T")),
        };

        Company company = new Company(employeeList, employeeArray);
        XMLDocument document = mapper.toXMLDocument(company);
        System.out.println(document.toXMLString());
    }
}