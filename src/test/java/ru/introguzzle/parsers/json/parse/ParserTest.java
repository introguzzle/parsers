package ru.introguzzle.parsers.json.parse;

import org.junit.Test;
import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.JSONObject;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public abstract class ParserTest {
    public abstract Parser getParser();

    @Test
    public void test_simple_case() {
        String data = "{\"string_value\":\"Hello, World!\",\"integer_value\":42,\"float_value\":3.1415,\"boolean_true\":true,\"boolean_false\":false,\"null_value\":null,\"object_value\":{\"nested_string\":\"Nested\",\"nested_integer\":123},\"array_of_primitives\":[1,\"two\",false],\"array_of_objects\":[{\"id\":1,\"name\":\"Alice\"},{\"id\":2,\"name\":\"Bob\"}]}";
        JSONObject o = getParser().parse(data, JSONObject.class);
        System.out.println(o.toJSONString());

        String stringValue = o.get("string_value", String.class);
        Number integerValue = o.get("integer_value", Number.class);
        Number floatValue = o.get("float_value", Number.class);
        Boolean booleanTrue = o.get("boolean_true", Boolean.class);
        Boolean booleanFalse = o.get("boolean_false", Boolean.class);
        Object nullValue = o.get("null_value", Object.class);
        JSONObject objectValue = o.get("object_value", JSONObject.class);

        assertEquals("Hello, World!", stringValue);
        assertEquals(42, integerValue.intValue());
        assertEquals(3.1415, floatValue.doubleValue(), 0.0001);
        assertTrue(booleanTrue);
        assertFalse(booleanFalse);
        assertNull(nullValue);

        String nestedString = objectValue.get("nested_string", String.class);
        Number nestedInteger = objectValue.get("nested_integer", Number.class);
        assertEquals("Nested", nestedString);
        assertEquals(123, nestedInteger.intValue());

        JSONArray arrayOfPrimitives = o.get("array_of_primitives", JSONArray.class);
        assertEquals(1, arrayOfPrimitives.get(0, Number.class).intValue());
        assertEquals("two", arrayOfPrimitives.get(1, String.class));
        assertFalse(arrayOfPrimitives.get(2, Boolean.class));

        JSONArray arrayOfObjects = o.get("array_of_objects", JSONArray.class);
        JSONObject firstObject = arrayOfObjects.get(0, JSONObject.class);
        JSONObject secondObject = arrayOfObjects.get(1, JSONObject.class);

        assertEquals(1, firstObject.get("id", Number.class).intValue());
        assertEquals("Alice", firstObject.get("name", String.class));

        assertEquals(2, secondObject.get("id", Number.class).intValue());
        assertEquals("Bob", secondObject.get("name", String.class));
    }

    @Test
    public void test_simple_case_async() {
        String data = "{\"string_value\":\"Hello, World!\",\"integer_value\":42,\"float_value\":3.1415,\"boolean_true\":true,\"boolean_false\":false,\"null_value\":null,\"object_value\":{\"nested_string\":\"Nested\",\"nested_integer\":123},\"array_of_primitives\":[1,\"two\",false],\"array_of_objects\":[{\"id\":1,\"name\":\"Alice\"},{\"id\":2,\"name\":\"Bob\"}]}";
        JSONObject o = getParser().parseAsync(data, JSONObject.class)
                .orTimeout(1000, TimeUnit.MILLISECONDS)
                .thenApply(j -> {
                    System.out.println(j);
                    return j;
                }).join();

        String stringValue = o.get("string_value", String.class);
        Number integerValue = o.get("integer_value", Number.class);
        Number floatValue = o.get("float_value", Number.class);
        Boolean booleanTrue = o.get("boolean_true", Boolean.class);
        Boolean booleanFalse = o.get("boolean_false", Boolean.class);
        Object nullValue = o.get("null_value", Object.class);
        JSONObject objectValue = o.get("object_value", JSONObject.class);

        assertEquals("Hello, World!", stringValue);
        assertEquals(42, integerValue.intValue());
        assertEquals(3.1415, floatValue.doubleValue(), 0.0001);
        assertTrue(booleanTrue);
        assertFalse(booleanFalse);
        assertNull(nullValue);

        String nestedString = objectValue.get("nested_string", String.class);
        Number nestedInteger = objectValue.get("nested_integer", Number.class);
        assertEquals("Nested", nestedString);
        assertEquals(123, nestedInteger.intValue());

        JSONArray arrayOfPrimitives = o.get("array_of_primitives", JSONArray.class);
        assertEquals(1, arrayOfPrimitives.get(0, Number.class).intValue());
        assertEquals("two", arrayOfPrimitives.get(1, String.class));
        assertFalse(arrayOfPrimitives.get(2, Boolean.class));

        JSONArray arrayOfObjects = o.get("array_of_objects", JSONArray.class);
        JSONObject firstObject = arrayOfObjects.get(0, JSONObject.class);
        JSONObject secondObject = arrayOfObjects.get(1, JSONObject.class);

        assertEquals(1, firstObject.get("id", Number.class).intValue());
        assertEquals("Alice", firstObject.get("name", String.class));

        assertEquals(2, secondObject.get("id", Number.class).intValue());
        assertEquals("Bob", secondObject.get("name", String.class));
    }

    @Test
    public void test_empty_json_object() {
        String data = "{}";
        JSONObject o = getParser().parse(data, JSONObject.class);
        assertNotNull(o);
        assertTrue(o.isEmpty());
    }

    @Test
    public void test_empty_json_array() {
        String data = "[]";
        JSONArray o = getParser().parse(data, JSONArray.class);
        assertNotNull(o);
        assertTrue(o.isEmpty());
    }

    @Test
    public void test_empty_string() {
        String data = "";
        String o = getParser().parse(data, String.class);
        assertNotNull(o);
        assertTrue(o.isEmpty());
    }

    @Test
    public void test_null() {
        String s = getParser().parse(null, String.class);
        Boolean b = getParser().parse(null, Boolean.class);

        assertNull(s);
        assertNull(b);
    }

    @Test(expected = JSONParseException.class)
    public void test_invalid_json1() {
        String data = "{\"string_value\":\"Hello, World!\",\"integer_value\":42,,,,,}";
        JSONObject object = getParser().parse(data, JSONObject.class);
        System.out.println(object.toJSONString());
    }

    @Test(expected = JSONParseException.class)
    public void test_invalid_json2() {
        String data = "{\"string_value\":\"Hello, World!\",\"integer_value\":42&;^}";
        JSONObject object = getParser().parse(data, JSONObject.class);
        System.out.println(object.toJSONString());
    }

    @Test
    public void test_json_array() {
        String data = "[{\"id\":1,\"name\":\"Alice^^^^,m,mmn,\"},{\"id\":2,\"name\":\"Bob\"}]";
        JSONArray array = getParser().parse(data, JSONArray.class);
        assertNotNull(array);
        assertEquals(2, array.size());

        JSONObject firstObject = array.get(0, JSONObject.class);
        JSONObject secondObject = array.get(1, JSONObject.class);

        assertEquals(1, firstObject.get("id", Number.class).intValue());
        assertEquals("Alice^^^^,m,mmn,", firstObject.get("name", String.class));

        assertEquals(2, secondObject.get("id", Number.class).intValue());
        assertEquals("Bob", secondObject.get("name", String.class));
    }
}