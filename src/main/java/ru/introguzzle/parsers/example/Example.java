package ru.introguzzle.parsers.example;

import lombok.AllArgsConstructor;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.xml.entity.XMLDocument;
import ru.introguzzle.parsers.xml.entity.annotation.XMLEntity;
import ru.introguzzle.parsers.xml.entity.annotation.XMLField;
import ru.introguzzle.parsers.xml.entity.annotation.XMLRoot;
import ru.introguzzle.parsers.xml.entity.type.XMLType;
import ru.introguzzle.parsers.xml.mapping.deserialization.ObjectMapper;
import ru.introguzzle.parsers.xml.mapping.serialization.Bindable;
import ru.introguzzle.parsers.xml.mapping.serialization.XMLMapper;
import ru.introguzzle.parsers.xml.parse.XMLParser;

import java.util.List;

public class Example {
    @AllArgsConstructor
    @XMLEntity(constructorArguments = {
            @ConstructorArgument("id"),
            @ConstructorArgument("name"),
            @ConstructorArgument("address"),
            @ConstructorArgument("employees")
    })
    @XMLRoot("Company")
    public static class Company implements Bindable {
        @XMLField(type = XMLType.ATTRIBUTE, name = "Id")
        int id;

        @XMLField(type = XMLType.ELEMENT, name = "Name")
        String name;

        @XMLField(type = XMLType.ELEMENT, name = "Address")
        Address address;

        @XMLField(type = XMLType.ELEMENT, name = "EmployeeList", element = "Employee")
        List<Employee> employees;

        @Override
        public XMLDocument toXMLDocument() {
            return Bindable.super.toXMLDocument();
        }
    }

    @AllArgsConstructor
    @XMLEntity(constructorArguments = {
            @ConstructorArgument("id"),
            @ConstructorArgument("street"),
            @ConstructorArgument("city")
    })
    @XMLRoot("Address")
    public static class Address {
        @XMLField(type = XMLType.ATTRIBUTE, name = "Id")
        int id;

        @XMLField(type = XMLType.ELEMENT, name = "Street")
        String street;

        @XMLField(type = XMLType.ELEMENT, name = "City")
        String city;
    }

    @AllArgsConstructor
    @XMLEntity(constructorArguments = {
            @ConstructorArgument("id"),
            @ConstructorArgument("firstName"),
            @ConstructorArgument("lastName"),
            @ConstructorArgument("salary")
    })
    @XMLRoot("Employee")
    public static class Employee {
        @XMLField(type = XMLType.ATTRIBUTE, name = "Id")
        Long id;

        @XMLField(type = XMLType.ELEMENT, name = "FirstName")
        String firstName;

        @XMLField(type = XMLType.ELEMENT, name = "LastName")
        String lastName;

        @XMLField(type = XMLType.ELEMENT, name = "Salary")
        float salary;
    }

    public static void main(String[] args) {
        String data =
                """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <Company Id="1">
                            <Name>Example Corp</name>
                            <Address Id="100">
                                <Street>123 Main St</Street>
                                <City>Metropolis</City>
                            </Address>
                            <EmployeeList>
                                <Employee Id="100">
                                    <FirstName>John</FirstName>
                                    <LastName>Doe</LastName>
                                    <Salary>50000.0</Salary>
                                </Employee>
                                <Employee Id="100">
                                    <FirstName>Jane</FirstName>
                                    <LastName>Smith</LastName>
                                    <Salary>60000.0</Salary>
                                </Employee>
                            </EmployeeList>
                        </Company>""";

        XMLParser parser = new XMLParser();
        XMLDocument document = parser.parse(data);

        // Obtain XMLDocument to object mapper
        ObjectMapper objectMapper = ObjectMapper.newMapper();

        // Obtain object to XMLDocument mapper
        XMLMapper mapper = XMLMapper.newMapper();

        // Bind mappers to class
        mapper.bindTo(Company.class);
        objectMapper.bindTo(Company.class);

        // Use methods of mappers directly on objects
        Company company = document.toObject(Company.class);
        XMLDocument after = company.toXMLDocument();
    }
}
