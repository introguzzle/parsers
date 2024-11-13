# parsers

# Overview
parsers is a lightweight and flexible Java library for parsing JSON strings into Java objects. It supports various data types, including JSONObject, JSONArray, and primitive types. With a simple interface for validation and conversion, it streamlines the process of working with JSON data in Java applications.

# Features
- 
- Flexible Parsing: Parse JSON strings into various Java types, including custom objects.
- POJO Mapping: Seamlessly map between JSONObject and Plain Old Java Objects (POJOs), enabling easy conversion between JSON data and Java objects.- Extensible Conversion: Customize conversion logic through the Converter interface.
- Lightweight: Minimal dependencies and a small footprint, making it ideal for inclusion in a wide range of projects.

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
        String data = "{\"name\": \"name\", \"age\": 17, \"double\": 333.3}";
        JSONParser parser = new JSONParser();
        JSONObject object = parser.parse(data, JSONObject.class);
        
        System.out.println(object.getString("name"));
        System.out.println(object.get("age", Number.class).intValue());
        System.out.println(object.get("float", Number.class).doubleValue());
    }
}
```
### Mapping a JSONObject to a POJO

```java
import lombok.AllArgsConstructor;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.json.JSONMapper;
import ru.introguzzle.parsers.json.JSONMapperImpl;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.json.entity.annotation.JSONEntity;
import ru.introguzzle.parsers.json.entity.annotation.JSONField;
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

        JSONMapper mapper = new JSONMapperImpl();
        Person person = mapper.toObject(object, Person.class);

        System.out.println(person.name); // Output: Jane Smith
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