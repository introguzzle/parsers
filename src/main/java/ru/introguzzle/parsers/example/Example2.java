package ru.introguzzle.parsers.example;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeToken;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.json.entity.annotation.JSONEntity;
import ru.introguzzle.parsers.json.mapping.deserialization.ObjectMapper;
import ru.introguzzle.parsers.json.mapping.serialization.JSONMapper;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class Example2 {
    @RequiredArgsConstructor
    @JSONEntity(constructorArguments = {
            @ConstructorArgument("first"),
            @ConstructorArgument("second")
    })
    @ToString
    public static class Box<A, B> {
        final A first;
        final B second;
    }

    @RequiredArgsConstructor
    @JSONEntity(constructorArguments = {
            @ConstructorArgument("values"),
            @ConstructorArgument("integers")
    })
    @ToString
    public static class Entity<T> {
        final Map<String, T> values;
        final List<Integer> integers;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        Type type = new TypeToken<Box<Integer, Entity<Boolean>>>() {}.getType();

        Map<String, Boolean> map = Map.of("1", true, "2", true, "3", true, "4", true, "5", true);
        Box<Integer, Entity<Boolean>> box = new Box<>(10, new Entity<>(map, List.of(99, 111, 333)));
        JSONObject object = JSONMapper.newMapper().toJSONObject(box);
        System.out.println(object.toJSONString());

        Box<Integer, Entity<Boolean>> after = (Box<Integer, Entity<Boolean>>) ObjectMapper.newMethodHandleMapper()
                .toObject(object, type);
        System.out.println(after);
    }
}
