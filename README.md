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
Jetbrains Annotations
Lombok

# Getting Started
### JSON related classes overview
- JSONArray - represents JSON array
- JSONObject - represents JSON object
- JSONEntity - class-level annotation that defines mapping configuration
- JSONField - field-level annotation
- JSONType - enum that represents all possible types in JSON according to specification
- JSONParser - parser that parses strings into corresponding type according to JSONType
- JSONMapper - class that is responsible for mapping objects into JSONObject instances
- ObjectMapper - class that is responsible for mapping JSONObject instance into other data classes

### XML related classes overview
- XMLElement - represents XML element
- XMLDocument - represents XML document
- XMLAttribute - represents XML attribute
- XMLEntity - class-level annotation that defines mapping configuration
- XMLField - field-level annotation
- XMLParser - parser that parses strings into XMLDocument instances
- XMLMapper - class that is responsible for mapping objects into XMLDocument instances
- ObjectMapper - class that is responsible for mapping XMLDocument instance into other data classes

### Parsing a JSON string to a JSONObject

```java
import ru.introguzzle.parsers.json.JSONParser;
import ru.introguzzle.parsers.json.entityUnion.JSONObject;
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
package ru.introguzzle.parsers.example;

import lombok.AllArgsConstructor;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeToken;
import ru.introguzzle.parsers.json.entityUnion.JSONObject;
import ru.introguzzle.parsers.json.entityUnion.annotation.JSONEntity;
import ru.introguzzle.parsers.json.entityUnion.annotation.JSONField;
import ru.introguzzle.parsers.json.mapping.deserialization.ObjectMapper;
import ru.introguzzle.parsers.json.mapping.serialization.JSONMapper;
import ru.introguzzle.parsers.json.parse.JSONParser;

import java.lang.reflect.Type;

public class Example {
    @AllArgsConstructor
    @JSONEntity(constructorArguments = {
            @ConstructorArgument("value"),
            @ConstructorArgument("name"),
            @ConstructorArgument("age")
    })
    public static class Person<T> {
        final T value;

        @JSONField(name = "renamed_1")
        final String name;

        @JSONField(name = "renamed_2")
        final int age;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        String data = "{\"value\": true, \"renamed_1\": \"name\", \"renamed_2\": 22}";

        JSONParser parser = new JSONParser();
        JSONObject object = parser.parse(data, JSONObject.class);
        System.out.println(object.toJSONString());

        ObjectMapper objectMapper = ObjectMapper.newMethodHandleMapper();
        // or use reflection based mapper
        // ObjectMapper objectMapper = ObjectMapper.newReflectionMapper();

        Type personType = new TypeToken<Person<Boolean>>() {}.getType();

        Person<Boolean> person = (Person<Boolean>) objectMapper.toObject(object, personType);
        JSONObject after = JSONMapper.newMapper().toJSONObject(person);

        System.out.println(after.toJSONString());
    }
}
```
### Complex example
```java
package ru.introguzzle.parsers.example;

import lombok.AllArgsConstructor;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.xml.entityUnion.XMLDocument;
import ru.introguzzle.parsers.xml.entityUnion.annotation.XMLEntity;
import ru.introguzzle.parsers.xml.entityUnion.annotation.XMLField;
import ru.introguzzle.parsers.xml.entityUnion.annotation.XMLRoot;
import ru.introguzzle.parsers.xml.entityUnion.type.XMLType;
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