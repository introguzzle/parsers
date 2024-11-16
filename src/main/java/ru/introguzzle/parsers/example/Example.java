package ru.introguzzle.parsers.example;

import lombok.AllArgsConstructor;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeToken;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.json.entity.annotation.JSONEntity;
import ru.introguzzle.parsers.json.entity.annotation.JSONField;
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