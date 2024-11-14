package ru.introguzzle.parsers.json.mapping.deserialization;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.junit.Test;
import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.common.annotation.ConstructorArgument;
import ru.introguzzle.parsers.json.entity.annotation.JSONEntity;
import ru.introguzzle.parsers.json.entity.annotation.JSONField;
import ru.introguzzle.parsers.json.mapping.MappingContext;
import ru.introguzzle.parsers.json.mapping.reference.StandardCircularReferenceStrategies;
import ru.introguzzle.parsers.json.mapping.serialization.JSONMapper;
import ru.introguzzle.parsers.json.mapping.type.JSONType;
import ru.introguzzle.parsers.xml.entity.XMLDocument;
import ru.introguzzle.parsers.foreign.Other;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ObjectMapperTest {
    private final ObjectMapper objectMapper = ObjectMapper.newMethodHandleMapper();
    private final JSONMapper jsonMapper = JSONMapper.newMapper();

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static final class Primitive {
        int intField;
        boolean flagField;
        char characterField;
        float floatField;
        double doubleField;
        byte byteField;
        short shortField;
        long longField;
        Primitive nextField;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static final class Boxed {
        Integer intField;
        Boolean flagField;
        Character characterField;
        String stringField;
        Float floatField;
        Double doubleField;
        Byte byteField;
        Short shortField;
        Long longField;
        Boxed nextField;
    }

    @Test
    public void test_primitive_types() {
        JSONObject next = new JSONObject();
        next.put("int_field", 10);
        next.put("flag_field", true);
        next.put("character_field", "a");
        next.put("float_field", 1.2f);
        next.put("double_field", 2.2d);
        next.put("byte_field", (byte) 1);
        next.put("short_field", (short) 1);
        next.put("long_field", 1L);

        JSONObject object = new JSONObject();
        object.put("int_field", 4665666);
        object.put("flag_field", false);
        object.put("character_field", "b");
        object.put("float_field", 112.2f);
        object.put("double_field", 253.25454d);
        object.put("byte_field", (byte) 7);
        object.put("short_field", (short) 4);
        object.put("long_field", 33333L);
        object.put("next_field", next);

        Primitive instance = objectMapper.toObject(object, Primitive.class);
        System.out.println(instance);
    }

    @Test
    public void test_boxed_types() {
        JSONObject next = new JSONObject();
        next.put("int_field", 10);
        next.put("flag_field", true);
        next.put("character_field", "a");
        next.put("string_field", "test");
        next.put("float_field", 1.2f);
        next.put("double_field", 2.2d);
        next.put("byte_field", (byte) 1);
        next.put("short_field", (short) 1);
        next.put("long_field", 1L);

        JSONObject object = new JSONObject();
        object.put("int_field", 666666);
        object.put("flag_field", false);
        object.put("character_field", "b");
        object.put("string_field", "test_string");
        object.put("float_field", 112.2f);
        object.put("double_field", 253.25454d);
        object.put("byte_field", (byte) 7);
        object.put("short_field", (short) 4);
        object.put("long_field", 33333L);
        object.put("next_field", next);

        Boxed instance = objectMapper.toObject(object, Boxed.class);
        System.out.println(instance);
    }

    @SuppressWarnings("ALL")
    @NoArgsConstructor
    @Getter
    public static class Circular {
        private int field;
        private Circular circular;
    }

    @Test
    @SuppressWarnings("ALL")
    public void test_circular_reference() {
        JSONObject object = new JSONObject();
        object.put("circular", object);

        Circular instance = objectMapper.toObject(object, Circular.class);
        assertEquals(instance.circular, instance);
        System.out.println(instance);

        JSONObject json = jsonMapper.toJSONObject(instance, new MappingContext(
                StandardCircularReferenceStrategies.USE_SPECIAL_OBJECT
        ));

        System.out.println(json.toJSONString());

        Circular after = objectMapper.toObject(json, Circular.class);
        System.out.println(after);
        assertEquals(after.circular.circular, after.circular);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @ToString
    public static class Person {
        int ageAge;
        String nameName;
        List<Person> personList;
    }

    @Test
    @SuppressWarnings("ALL")
    public void test_list() {
        Person initial = new Person(
                10, "SUPPRESS ALL", List.of(
                        new Person(33, "AAGREGREGREGREGFJEIWJOEFIW", List.of()),
                        new Person(20, "ZZZZZ", List.of()))
        );

        JSONObject initialToJson = jsonMapper.toJSONObject(initial, new MappingContext(
                StandardCircularReferenceStrategies.USE_SPECIAL_OBJECT
        ));

        System.out.println(initialToJson.toJSONString() + "\n");

        Person initialToJsonToPerson = objectMapper.toObject(initialToJson, Person.class);
        Person first = initialToJsonToPerson.personList.getFirst();

        System.out.println("\n\nPERSON");
        System.out.println(initialToJsonToPerson);
        System.out.println("\n\nPERSON");

        JSONObject initialToJsonToPersonToJson = jsonMapper.toJSONObject(initialToJsonToPerson, new MappingContext(
                StandardCircularReferenceStrategies.USE_SPECIAL_OBJECT
        ));

        System.out.println(initialToJsonToPersonToJson.toJSONString());
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @ToString
    public static final class POJO {
        private int age;
        private String firstName;
        private String lastName;
        private String address;
    }

    @Test
    public void test_mapping_array() {
        POJO[] pojos = new POJO[]{
            new POJO(10, "FI", "LA", "ADDRESS 1"),
            new POJO(200, "FIRST", "LAST", "ADDRESS 2"),
            new POJO(200, "FIRST", "LAST", "ADDRESS 2")
        };

        JSONArray array = jsonMapper.toJSONArray(pojos, MappingContext.getDefault());

        System.out.println(array.toJSONString());

        POJO[] after = objectMapper.toArray(array, POJO[].class);
        System.out.println(Arrays.toString(after));

        List<POJO> afterList = objectMapper.toCollection(array, POJO.class, LinkedList::new);
        System.out.println(afterList);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static final class Q {
        @JSONField(type = JSONType.ARRAY, name = "queue_renamed")
        public Queue<Integer> queue;
        @JSONField(type = JSONType.ARRAY, name = "list_renamed")
        public LinkedList<String> list;
    }

    @Test
    public void test_queue() {
        Queue<Integer> queue = new LinkedList<>();
        queue.add(1);
        queue.add(2);

        LinkedList<String> list = new LinkedList<>();
        list.add("a");
        list.add("b");

        Q instance = new Q(queue, list);

        JSONObject object = jsonMapper.toJSONObject(instance, MappingContext.getDefault());

        System.out.println(object.toJSONString());

        Q after = objectMapper.withTypeHandler(Queue.class, (o, _) -> new LinkedList<>((JSONArray) o))
                .withTypeHandler(LinkedList.class, (_, _) -> null)
                .toObject(object, Q.class);

        System.out.println(after);

        assertEquals(after.queue, instance.queue);
        assertNull(after.list);
    }

    @AllArgsConstructor
    @Getter
    @ToString
    @JSONEntity(constructorArguments = {
            @ConstructorArgument("age"),
            @ConstructorArgument("name")
    })
    @SuppressWarnings("ALL")
    public static final class Final1 {
        private final int age;
        private final String name;
    }

    @Test
    public void test_constructor_with_args() {
        Final1 instance = new Final1(10, "NAMEEEEEEE");

        System.out.println("instance : " + instance);

        JSONObject object = jsonMapper.toJSONObject(instance, MappingContext.getDefault());

        Final1 after = objectMapper.toObject(object, Final1.class);
        System.out.println("after : " + after);
    }

    @AllArgsConstructor
    @Getter
    @ToString
    @JSONEntity(constructorArguments = {
            @ConstructorArgument("age"),
            @ConstructorArgument("name"),
            @ConstructorArgument("next")
    })
    @SuppressWarnings("ALL")
    public static final class ComplexConstructor {
        private final int age;
        private final String name;
        private final ComplexConstructor next;
    }

    @Test
    public void test_complex_constructor() {
        ComplexConstructor next = new ComplexConstructor(222, "ZZZZZ", null);
        ComplexConstructor instance = new ComplexConstructor(10, "NAMEEEEEEE", next);
        System.out.println("instance : " + instance);

        JSONObject object = jsonMapper.toJSONObject(instance, MappingContext.getDefault());
        System.out.println(object.toJSONString());

        XMLDocument document = object.toXMLDocument();
        System.out.println(document.toXMLString());

        ComplexConstructor after = objectMapper.toObject(object, ComplexConstructor.class);
        System.out.println("after : " + after);
    }

    @AllArgsConstructor
    @Getter
    @ToString
    @JSONEntity(constructorArguments = {
            @ConstructorArgument("name")
    })
    @SuppressWarnings("ALL")
    public static final class Final3 {
        private final int age;
        private final String name;
    }

    @Test
    public void test_constructor_with_args_invalid_args() {
        Final3 instance = new Final3(10, "NAMEEEEEEE");
        System.out.println("instance : " + instance);

        JSONObject object = jsonMapper.toJSONObject(instance, MappingContext.getDefault());

        assertThrows(Exception.class, () -> objectMapper.toObject(object, Final3.class));
    }
    @AllArgsConstructor
    @Getter
    @ToString
    @JSONEntity(constructorArguments = {
            @ConstructorArgument("firstName"),
            @ConstructorArgument("lastName")
    })
    @SuppressWarnings("ALL")
    public static class Char {
        private final char[] firstName;
        private final char[] lastName;
    }

    @Getter
    @ToString
    @JSONEntity(constructorArguments = {
            @ConstructorArgument("firstName"),
            @ConstructorArgument("lastName"),
            @ConstructorArgument("address"),
            @ConstructorArgument("numbers")
    })
    @SuppressWarnings("ALL")
    @EqualsAndHashCode
    public static class InheritingFromChar extends Char {
        private final char[] address;
        private final int[] numbers;

        public InheritingFromChar(char[] firstName,
                                  char[] lastName,
                                  char[] address,
                                  int[] numbers) {
            super(firstName, lastName);
            this.address = address;
            this.numbers = numbers;
        }
    }

    @Test
    @SuppressWarnings("ALL")
    public void test_type_handler() {
        InheritingFromChar instance1 = new InheritingFromChar(
                "ABCDE".toCharArray(),
                "LASTNAME".toCharArray(),
                "ADDRESS___________".toCharArray(),
                new int[]{1, 2, 3, 4, 5}
        );

        InheritingFromChar instance2 = new InheritingFromChar(
                "ZZZZZZZZZZZZZZ".toCharArray(),
                "LASTNAME11111111111".toCharArray(),
                "NO 58948594".toCharArray(),
                new int[]{5, 4, 3, 2, 1}
        );

        InheritingFromChar[] instances = new InheritingFromChar[]{instance1, instance2};
        System.out.println("\nBEFORE\n");
        System.out.println(Arrays.toString(instances));

        JSONMapper jsonMapper = JSONMapper.newMapper()
                .withTypeHandler(char[].class, String::new)
                .withTypeHandler(int[].class, ints -> Arrays.stream(ints)
                        .boxed()
                        .map(Object::toString)
                        .collect(Collectors.joining()));

        JSONArray object = jsonMapper.toJSONArray(instances, MappingContext.getDefault());

        System.out.println("\nJSON\n");
        System.out.println(object.toJSONString());
        InheritingFromChar[] after = objectMapper
                .withTypeHandler(char[].class, (o, _) -> {
                    String s = (String) o;
                    return s.toCharArray();
                })
                .withTypeHandler(int[].class, (o, _) -> {
                    String s = (String) o;
                    int[] integers = new int[s.length()];
                    for (int i = 0; i < integers.length; i++) {
                        integers[i] = Integer.parseInt(Character.toString(s.charAt(i)));
                    }

                    return integers;
                })
                .toArray(object, InheritingFromChar[].class);

        System.out.println("\nAFTER\n");
        System.out.println(Arrays.toString(after));
        assertArrayEquals(instances, after);
    }

    @Test
    public void test_other_package() {
        Other before = new Other("1337");
        JSONObject object = jsonMapper.toJSONObject(before, MappingContext.getDefault());

        Other after = objectMapper.toObject(object, Other.class);
        assertEquals(before, after);
    }
}