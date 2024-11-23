package ru.introguzzle.parsers.example;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.common.annotation.ConstructorArguments;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeToken;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.json.entity.annotation.JSONEntity;
import ru.introguzzle.parsers.json.entity.annotation.JSONField;
import ru.introguzzle.parsers.json.mapping.deserialization.ObjectMapper;
import ru.introguzzle.parsers.json.mapping.serialization.JSONMapper;
import ru.introguzzle.parsers.xml.entity.XMLDocument;
import ru.introguzzle.parsers.xml.entity.annotation.XMLEntity;
import ru.introguzzle.parsers.xml.entity.annotation.XMLField;
import ru.introguzzle.parsers.xml.entity.type.XMLType;
import ru.introguzzle.parsers.xml.mapping.serialization.XMLMapper;

import java.lang.reflect.Type;

public class Example {
    @RequiredArgsConstructor
    @JSONEntity
    @XMLEntity
    @ToString
    @ConstructorArguments({
            @ConstructorArgument("a"),
            @ConstructorArgument("b")
    })
    public static class GenericTyped<A, B> {
        @JSONField(name = "type_a")
        @XMLField(name = "XML_TYPE_A", type = XMLType.ELEMENT)
        private final A a;

        @JSONField(name = "type_b")
        @XMLField(name = "XML_TYPE_B", type = XMLType.ELEMENT)
        private final B b;
    }

    @RequiredArgsConstructor
    @JSONEntity
    @ToString
    @ConstructorArguments({
            @ConstructorArgument("value"),
            @ConstructorArgument("typed")
    })
    @XMLEntity
    public static class Generic<A, B> {
        @JSONField(name = "value_renamed")
        @XMLField(type = XMLType.ELEMENT)
        private final byte value;

        @JSONField(name = "typed_renamed")
        @XMLField(type = XMLType.ELEMENT)
        private final GenericTyped<A, B> typed;
    }

    @RequiredArgsConstructor
    @Getter
    @XMLEntity
    @JSONEntity
    @ToString
    @ConstructorArguments({
            @ConstructorArgument("age"),
            @ConstructorArgument("salary"),
            @ConstructorArgument("name"),
            @ConstructorArgument("address")
    })
    public static class Person {
        @XMLField(type = XMLType.ELEMENT)
        private final int age;

        @XMLField(type = XMLType.ELEMENT)
        private final double salary;

        @XMLField(type = XMLType.ELEMENT)
        private final String name;

        @XMLField(type = XMLType.ELEMENT)
        private final String address;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Throwable {
        Person person = new Person(10, 1337d, "name", "address");
        Generic<Generic<Integer, Person>, String> generic = new Generic<>(
                (byte) 1,
                new GenericTyped<>(new Generic<>((byte) 2, new GenericTyped<>(2, person)), "1")
        );

        System.out.println(generic);

        JSONObject jsonObject = JSONMapper.newMapper().toJSONObject(generic);
        XMLDocument document = XMLMapper.newMapper().toXMLDocument(generic);

        System.out.println(document.toXMLString());

        Type type = new TypeToken<Generic<Generic<Integer, Person>, String>>() {}.getType();
        var after = (Generic<Generic<Integer, Person>, String>) ObjectMapper
                .newMethodHandleMapper()
                .toObject(jsonObject, type);

        byte value = after.value;
        GenericTyped<Generic<Integer, Person>, String> typed = after.typed;

        System.out.println(after);
    }
}
