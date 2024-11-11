package ru.introguzzle.parsers.json.mapping.serialization;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import ru.introguzzle.parsers.foreign.Foreign;
import ru.introguzzle.parsers.foreign.Other;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.common.annotation.Excluded;
import ru.introguzzle.parsers.json.entity.annotation.JSONEntity;
import ru.introguzzle.parsers.json.entity.annotation.JSONField;
import ru.introguzzle.parsers.json.mapping.Data;
import ru.introguzzle.parsers.json.mapping.JSONObjectConvertable;
import ru.introguzzle.parsers.json.mapping.MappingContext;
import ru.introguzzle.parsers.common.mapping.MappingException;
import ru.introguzzle.parsers.json.mapping.type.JSONType;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class JSONMapperTest {
    private final JSONMapper mapper = new JSONMapperImpl();

    @Test
    public void test_transient() {
        Data.Transient data = new Data.Transient("John Doe", 42);
        JSONObject resultObject = mapper.toJSONObject(data, MappingContext.getDefault());

        assertTrue(resultObject.isEmpty());

        System.out.println(resultObject.toJSONString());
    }

    @Test
    public void test_inheriting_with_annotations() {
        Data.InheritingWithAnnotations data = new Data.InheritingWithAnnotations("stringValue", (byte) 1, 333.3);
        JSONObject resultObject = mapper
                .toJSONObject(data, MappingContext.getDefault());

        assertEquals(resultObject.get("original_string", String.class), "stringValue");
        assertEquals(resultObject.get("original_byte", Number.class).byteValue(), (byte) 1);
        assertEquals(resultObject.get("inheriting_double", Number.class).doubleValue(), 333.3, 1);

        System.out.println(resultObject.toJSONString());
    }

    @Test
    public void test_inheriting() {
        Data.Inheriting data = new Data.Inheriting("stringValue", (byte) 25, 954.313);
        JSONObject resultObject = mapper.toJSONObject(data, MappingContext.getDefault());

        assertEquals(resultObject.get("original_string", String.class), "stringValue");
        assertEquals(resultObject.get("original_byte", Number.class).byteValue(), (byte) 25);
        assertEquals(resultObject.get("inheriting_double", Number.class).doubleValue(), 954.313, 1);

        System.out.println(resultObject.toJSONString());
    }

    @Test
    public void test_string() {
        @AllArgsConstructor
        class Local {
            @JSONField(name = "other", type = JSONType.NUMBER)
            public String number;
            public Local next;
        }

        Local instance = new Local("1337", new Local("99999", null));
        JSONObject object = mapper.toJSONObject(instance, MappingContext.getDefault());

        assertEquals(1337.0, object.getNumber("other").doubleValue(), 0.00001);
        assertEquals(99999.0, object.getObject("next").getNumber("other").doubleValue(), 0.00001);

        System.out.println(object.toJSONString());
    }

    @Test
    public void test_node() {
        Data.Node grandChild = new Data.Node("grandChild", 13, 13, "email", null);
        Data.Node child = new Data.Node("child", 26, 26, "email", grandChild);
        Data.Node root = new Data.Node("root", 99, 99, "email", child);

        JSONObject rootObject = mapper.toJSONObject(root, MappingContext.getDefault());

        assertEquals(rootObject.getString("name"), "root");
        assertEquals(rootObject.getNumber("boxed_age").intValue(), 99);
        assertEquals(rootObject.getNumber("age").intValue(), 99);
        assertEquals(rootObject.getString("email"), "email");

        JSONObject childObject = rootObject.getObject("next");
        assertEquals(childObject.getString("name"), "child");
        assertEquals(childObject.getNumber("boxed_age").intValue(), 26);
        assertEquals(childObject.getNumber("age").intValue(), 26);
        assertEquals(childObject.getString("email"), "email");

        JSONObject grandChildObject = childObject.getObject("next");
        assertEquals(grandChildObject.getString("name"), "grandChild");
        assertEquals(grandChildObject.getNumber("boxed_age").intValue(), 13);
        assertEquals(grandChildObject.getNumber("age").intValue(), 13);
        assertEquals(grandChildObject.getString("email"), "email");

        System.out.println(rootObject.toJSONString());
    }

    @Test
    public void test_json_entity_annotation_all_excluded() {
        @JSONEntity(excluded = {
                @Excluded("valueValue1"),
                @Excluded("valueValue2"),
                @Excluded("valueValue3")
        })
        @Getter
        class None {
            int valueValue1;
            int valueValue2;
            int valueValue3;
        }

        None none = new None();

        JSONObject object = mapper.toJSONObject(none, MappingContext.getDefault());
        assertTrue(object.isEmpty());

        System.out.println(object.toJSONString());
    }

    @Test
    public void test_json_entity_annotation_some_excluded() {
        @JSONEntity(excluded = {
                @Excluded("valueValue1")
        })
        @Getter
        class None {
            final int valueValue1 = 10;
            final int valueValue2 = 20;
            final int valueValue3 = 30;
        }

        None none = new None();

        JSONObject object = mapper.toJSONObject(none, MappingContext.getDefault());

        assertEquals(object.getNumber("value_value_2").intValue(), 20);
        assertEquals(object.getNumber("value_value_3").intValue(), 30);
        assertEquals(2, object.size());

        System.out.println(object.toJSONString());
    }

    @Test
    public void test_array_1() {
        @AllArgsConstructor
        @Getter
        class Local {
            final int[] array;
        }

        Local instance = new Local(new int[]{11, 222, 3333});

        JSONObject object = mapper.toJSONObject(instance, MappingContext.getDefault());
        System.out.println(object.toJSONString());
    }

    @Test
    public void test_array_2() {
        @AllArgsConstructor
        @Getter
        class Local {
            final int[] intArray;
            final long[] longArray;
            final float[] floatArray;
            final double[] doubleArray;
            final short[] shortArray;
            final byte[] byteArray;
            final char[] charArray;
            final boolean[] booleanArray;

            final Integer[] boxedIntegerArray;
            final Long[] boxedLongArray;
            final Float[] boxedFloatArray;
            final Double[] boxedDoubleArray;
            final Short[] boxedShortArray;
            final Byte[] boxedByteArray;
            final Character[] boxedCharacterArray;
            final Boolean[] boxedBooleanArray;
        }

        Local instance = new Local(
                new int[] {1, 2, 3},
                new long[] {4L, 5L, 6L},
                new float[] {7.0f},
                new double[] {8.0, 9.0, 10.0, 11.0},
                new short[] {},
                new byte[] {12, 13},
                new char[] {'a', 'b', 'c'},
                new boolean[] {true, false},

                new Integer[] {100, 200, 300},
                new Long[] {400L, 500L},
                new Float[] {7.1f, 8.2f},
                new Double[] {9.3, 10.4},
                new Short[] {1, 2},
                new Byte[] {10, 20},
                new Character[] {'x', 'y'},
                new Boolean[] {true, false, true}
        );

        JSONObject object = mapper.toJSONObject(instance, MappingContext.getDefault());

        System.out.println(object.toJSONString());
    }

    @Test
    public void test_map() {
        @AllArgsConstructor
        class Person {
            final int age;
            final String name;
        }

        @AllArgsConstructor
        class Local {
            final Map<String, String> map1;
            final Map<String, Person> map2;
        }

        Map<String, String> map1 = new HashMap<>();
        map1.put("key1", "val2");
        map1.put("key2", "val3");

        Map<String, Person> map2 = new HashMap<>();
        map2.put("FPEWFPEW", new Person(10, "lowlife"));
        map2.put("EWGEWGEWEWPW21", new Person(99, "endthis"));

        Local instance = new Local(map1, map2);

        JSONObject object = mapper.toJSONObject(instance, MappingContext.getDefault());

        System.out.println(object.toJSONString());
    }

    @Test
    public void test_enums_collections_and_arrays() {
        Data.CollectionsData data = new Data.CollectionsData(
                new int[]{11, 222, 3333},
                List.of("val", "var", "interface"),
                EnumSet.of(Data.Gender.MALE, Data.Gender.FEMALE),
                Map.of("UP", Data.Direction.UP, "DOWN", Data.Direction.DOWN),
                List.of(new Date(), new Date(), new Date(132325454L))
        );

        JSONObject object = mapper.toJSONObject(data, MappingContext.getDefault());


        System.out.println(object.toJSONString());
    }

    @Test
    public void test_type_handlers() {
        var objectToJSONMapper = new JSONMapperImpl()
                .withTypeHandler(RuntimeException.class, _ -> "RuntimeException handler")
                .withTypeHandler(MappingException.class, _ -> "MappingException handler")
                .withTypeHandler(Class.class, _ -> "Class handler");

        @AllArgsConstructor
        @SuppressWarnings("unused")
        class Local {
            final static Throwable staticField = new Throwable();
            final Throwable throwable;
            final RuntimeException exception;
            final MappingException mappingException;
            final IOException ioException;
            final Class<? extends Local> type;
        }

        Local instance = new Local(
                new Throwable("Throwable handler"),
                new RuntimeException(),
                new MappingException(),
                new IOException("Throwable handler"),
                Local.class
        );

        JSONObject object = objectToJSONMapper.toJSONObject(instance, MappingContext.getDefault());
        assertEquals("Throwable handler", object.getString("throwable"));
        assertEquals("RuntimeException handler", object.getString("exception"));
        assertEquals("MappingException handler", object.getString("mapping_exception"));
        assertEquals("Throwable handler", object.getString("io_exception"));
        assertEquals("Class handler", object.getString("type"));

        System.out.println(object.toJSONString());
    }

    @AllArgsConstructor
    public static final class Implements implements JSONObjectConvertable {
        static final JSONObject OBJECT = new JSONObject();

        @Override
        public @NotNull JSONObject toJSONObject() {
            return OBJECT;
        }
    }

    @Test
    public void test_interface() {
        JSONObject object = mapper.toJSONObject(new Implements(), MappingContext.getDefault());
        assertEquals(object, Implements.OBJECT);
    }

    @Test
    public void test_injection() {
        mapper.bindTo(Other.class);
        Other other = new Other("1337");
        System.out.println(other.toJSONObject().toJSONString());
    }

    @Test
    public void test_injections() {
        JSONMapper mapper1 = new JSONMapperImpl()
                .withTypeHandler(String.class, _ -> "mapper1 handle");

        JSONMapper mapper2 = new JSONMapperImpl()
                .withTypeHandler(String.class, _ -> "mapper2 handle");

        mapper1.bindTo(Other.class);
        mapper2.bindTo(Set.of(Foreign.class, ru.introguzzle.parsers.foreign.Test.class));

        var test = new ru.introguzzle.parsers.foreign.Test("doesnt matter");
        Other other = new Other("doesnt matter");
        Foreign foreign = new Foreign("doesnt matter");

        System.out.println(test.toJSONObject().toJSONString());
        System.out.println(other.toJSONObject().toJSONString());
        System.out.println(foreign.toJSONObject().toJSONString());
    }
}