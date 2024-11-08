package ru.introguzzle.parsers.json.mapping;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static ru.introguzzle.parsers.common.mapping.ClassHierarchyTraverseUtilities.findMostGeneralMatch;
import static ru.introguzzle.parsers.common.mapping.ClassHierarchyTraverseUtilities.findMostSpecificMatch;

@SuppressWarnings("ALL")
public class ClassHierarchyTraverseUtilitiesTest {
    public static class Parent extends MappingException {}
    public static class Child extends Parent {}
    public static class Grand extends Child {}

    @Test
    public void test_bfs() {
        Map<Class<?>, Integer> map = new HashMap<>();
        map.put(Grand.class, 0);
        map.put(Child.class, 1);
        map.put(Parent.class, 2);
        map.put(MappingException.class, 3);

        assertEquals((Integer) 3, findMostSpecificMatch(map, MappingException.class).get());
    }

    @Test
    public void test_dfs() {
        Map<Class<?>, Integer> map = new HashMap<>();
        map.put(Grand.class, 0);
        map.put(Child.class, 1);
        map.put(Parent.class, 2);
        map.put(MappingException.class, 3);
        map.put(Throwable.class, 4);

        assertEquals((Integer) 4, findMostGeneralMatch(map, MappingException.class).get());
    }
}