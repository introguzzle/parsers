package ru.introguzzle.parsers.example;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.ExtensionMethod;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.common.mapping.AccessPolicy;
import ru.introguzzle.parsers.common.util.Streams;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.json.entity.annotation.JSONEntity;
import ru.introguzzle.parsers.json.entity.annotation.JSONField;
import ru.introguzzle.parsers.json.mapping.deserialization.ObjectMapper;
import ru.introguzzle.parsers.json.mapping.serialization.JSONMapper;

import java.util.ArrayList;
import java.util.List;

@ExtensionMethod(Streams.class)
public class Example4 {
    @RequiredArgsConstructor
    @JSONEntity(constructorArguments = {
            @ConstructorArgument("age"),
            @ConstructorArgument("name")
    })
    @ToString
    public static class Person {
        @JSONField(name = "static")
        private static final int STATIC = 100;
        private final int age;
        private final String name;
        private volatile int vlt;
        private transient int trs;
    }

    public static void main(String[] args) {
        List<Number> strings = new ArrayList<>();
        strings.add(3.0);
        strings.add(1);
        strings.add((byte) 3);

        List<Integer> integers = new ArrayList<>();
        integers.add(1);
        integers.add(2);

        strings.stream()
                .downcast(Integer.class)
                .append(integers)
                .toList()
                .forEach(System.out::println);

        Person person = new Person(666, "777");
        person.trs = 1;
        person.vlt = 2;

        JSONObject object = JSONMapper.newMapper().toJSONObject(person);
        System.out.println(object.toJSONString());

        Person after = (Person) ObjectMapper.newMethodHandleMapper().toObject(object, Person.class);
        System.out.println(after);
    }
}
