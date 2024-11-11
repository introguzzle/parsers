package ru.introguzzle.parsers.json.mapping;

import org.junit.Test;
import ru.introguzzle.parsers.common.mapping.ClassTraverser;
import ru.introguzzle.parsers.common.mapping.MappingException;
import ru.introguzzle.parsers.common.mapping.Traverser;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

@SuppressWarnings("ALL")
public class TraverserTest {
    private final Traverser<Class<?>> traverser = new ClassTraverser();

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

        assertEquals((Integer) 3, traverser.findMostSpecificMatch(map, MappingException.class).get());
    }

    @Test
    public void test_dfs() {
        Map<Class<?>, Integer> map = new HashMap<>();
        map.put(Grand.class, 0);
        map.put(Child.class, 1);
        map.put(Parent.class, 2);
        map.put(MappingException.class, 3);
        map.put(Throwable.class, 4);

        assertEquals((Integer) 4, traverser.findMostGeneralMatch(map, MappingException.class).get());
    }
}