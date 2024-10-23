package ru.introguzzle.parser.common.utilities;

import org.junit.Test;
import static org.junit.Assert.*;

public class NamingUtilitiesTest {

    @Test
    public void test1() {
        String string = "aA";
        String result = NamingUtilities.toSnakeCase(string);
        assertEquals("a_a", result);
    }

    @Test
    public void test2() {
        assertEquals(NamingUtilities.toSnakeCase("Z"), "z");
    }

    @Test
    public void test3() {
        assertEquals(NamingUtilities.toSnakeCase("z"), "z");
    }

    @Test
    public void test4() {
        assertEquals(NamingUtilities.toSnakeCase("camelCase"), "camel_case");
    }

    @Test
    public void test5() {
        assertEquals(NamingUtilities.toSnakeCase("TitleCaseNumber"), "title_case_number");
    }

    @Test
    public void test_null() {
        assertNull(NamingUtilities.toSnakeCase(null));
    }
}