package ru.introguzzle.parsers.common.mapping;

import lombok.experimental.UtilityClass;
import java.util.*;

@UtilityClass
public final class ClassHierarchyTraverseUtilities {

    /**
     *
     * Finds the most specific match for the given type in the provided map
     * by traversing the class hierarchy in a BFS manner.
     *
     * @param map The map containing class types as keys and associated values.
     * @param type The type to find the most general match for.
     * @param <T> The type of values in the map.
     * @return An Optional containing the most specific match, or empty if no match is found.
     */
    public static <T> Optional<T> findMostSpecificMatch(Map<Class<?>, ? extends T> map, Class<?> type) {
        // Breadth-First Search (BFS) for class hierarchy traversal
        Queue<Class<?>> queue = new LinkedList<>();
        Set<Class<?>> visited = new HashSet<>();

        queue.add(type);
        visited.add(type);

        while (!queue.isEmpty()) {
            Class<?> current = queue.poll();

            // Check if there's a match for the current class
            T match = map.get(current);
            if (match != null) {
                return Optional.of(match);
            }

            // Enqueue superclass
            Class<?> parent = current.getSuperclass();
            if (parent != null && visited.add(parent)) {
                queue.add(parent);
            }

            // Enqueue interfaces
            for (Class<?> interfaceType : current.getInterfaces()) {
                if (visited.add(interfaceType)) {
                    queue.add(interfaceType);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Finds the most general match (the highest ancestor) for the given type in the provided map
     * by traversing the class hierarchy in a depth-first manner.
     *
     * @param map The map containing class types as keys and associated values.
     * @param type The type to find the most general match for.
     * @param <T> The type of values in the map.
     * @return An Optional containing the most general match, or empty if no match is found.
     */
    public static <T> Optional<T> findMostGeneralMatch(Map<Class<?>, ? extends T> map, Class<?> type) {
        Set<Class<?>> visited = new HashSet<>();
        return doFindMostGeneralMatch(map, type, visited);
    }

    private static <T> Optional<T> doFindMostGeneralMatch(Map<Class<?>, ? extends T> map, Class<?> type, Set<Class<?>> visited) {
        // Base case: if the type has already been visited, we return empty to avoid cycles
        if (type == null || !visited.add(type)) {
            return Optional.empty();
        }

        // Check if the current class has a match
        T match = map.get(type);
        if (match != null) {
            // Continue looking up to see if there's a more general (ancestor) match
            Optional<T> parentMatch = doFindMostGeneralMatch(map, type.getSuperclass(), visited);
            if (parentMatch.isPresent()) {
                return parentMatch;  // prefer more general ancestor match
            }

            // Check interfaces if no superclass match is found
            for (Class<?> interfaceType : type.getInterfaces()) {
                Optional<T> interfaceMatch = doFindMostGeneralMatch(map, interfaceType, visited);
                if (interfaceMatch.isPresent()) {
                    return interfaceMatch;
                }
            }

            return Optional.of(match);
        }

        // Recursively check superclass
        Optional<T> superClassMatch = doFindMostGeneralMatch(map, type.getSuperclass(), visited);
        if (superClassMatch.isPresent()) {
            return superClassMatch;
        }

        // Recursively check interfaces if no superclass match is found
        for (Class<?> interfaceType : type.getInterfaces()) {
            Optional<T> interfaceMatch = doFindMostGeneralMatch(map, interfaceType, visited);
            if (interfaceMatch.isPresent()) {
                return interfaceMatch;
            }
        }

        // No match found in hierarchy
        return Optional.empty();
    }
}
