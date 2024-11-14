# parsers

# Overview
parsers is a lightweight and flexible Java library for parsing JSON strings into Java objects. It supports various data types, including JSONObject, JSONArray, and primitive types. With a simple interface for validation and conversion, it streamlines the process of working with JSON data in Java applications.

# Features
- Flexible Parsing: Parse JSON and XML strings into various Java types, including custom objects
- POJO Mapping: Seamlessly map between JSONObject/XMLDocument and Java objects
- Extensible Conversion: Customize conversion logic through the Converter interface
- Lightweight: Minimal dependencies and a small footprint, making it ideal for inclusion in a wide range of projects

# Installation
Add the following dependency to your pom.xml file (for Maven users):

```
<dependency>
<groupId>ru.introguzzle</groupId>
<artifactId>parsers</artifactId>
<version>1.0-SNAPSHOT</version>
</dependency>
```
For Gradle users, add the following to your build.gradle:

```
implementation 'ru.introguzzle:parsers:1.0-SNAPSHOT'
```
# Requirements
Java 22

# Getting Started
### Parsing a JSON string to a JSONObject

```java
import ru.introguzzle.parsers.json.JSONParser;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.json.parse.JSONParser;

public class Example {
    public static void main(String[] args) {
        String data = "{\"name\": \"name\", \"age\": 17, \"double_value\": 333.3}";
        JSONParser parser = new JSONParser();
        JSONObject object = parser.parse(data, JSONObject.class);
        
        String name = object.getString("name");
        Number age = object.get("age", Number.class);
        Number doubleValue = object.get("double_value", Number.class);
    }
}
```
### Mapping a JSONObject to a Java object and vice versa

```java
import lombok.AllArgsConstructor;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.json.JSONMapper;
import ru.introguzzle.parsers.json.JSONMapperImpl;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.json.entity.annotation.JSONEntity;
import ru.introguzzle.parsers.json.entity.annotation.JSONField;
import ru.introguzzle.parsers.json.mapping.deserialization.InvokeObjectMapper;
import ru.introguzzle.parsers.json.mapping.deserialization.ObjectMapper;
import ru.introguzzle.parsers.json.mapping.deserialization.ReflectionObjectMapper;
import ru.introguzzle.parsers.json.mapping.serialization.JSONMapper;
import ru.introguzzle.parsers.json.mapping.serialization.JSONMapperImpl;
import ru.introguzzle.parsers.json.parse.JSONParser;

public class Example {
    @AllArgsConstructor
    @JSONEntity(constructorArguments = {
            @ConstructorArgument("name"),
            @ConstructorArgument("age")
    })
    public static class Person {
        @JSONField(name = "renamed_1")
        String name;

        @JSONField(name = "renamed_2")
        int age;
    }

    public static void main(String[] args) {
        String data = "{\"renamed_1\": \"name\", \"renamed_2\": 99}";
        JSONParser parser = new JSONParser();
        JSONObject object = parser.parse(data, JSONObject.class);

        ObjectMapper objectMapper = ObjectMapper.newMethodHandleMapper();
        // or use reflection based mapper
        // ObjectMapper objectMapper = ObjectMapper.newReflectionMapper();

        Person person = objectMapper.toObject(object, Person.class);
        JSONObject after = JSONMapper.newMapper().toJSONObject(person);
    }
}
```
### Complex example
```java
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

```
## Documentation
For detailed information on how to use the library, refer to the API Documentation.

## Contributing
Contributions are welcome! Please read our Contributing Guidelines before submitting a pull request.

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Support
If you encounter any issues or have questions, please open an issue on GitHub Issues.