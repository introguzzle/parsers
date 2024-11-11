package ru.introguzzle.parsers.common.mapping;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

public class ClassTraverser implements Traverser<Class<?>> {
    @Override
    public <T> Optional<T> findMostSpecificMatch(@NotNull Map<? extends Class<?>, ? extends T> map, @NotNull Class<?> target) {
        Queue<Class<?>> queue = new LinkedList<>();
        Set<Class<?>> visited = new HashSet<>();

        queue.add(target);
        visited.add(target);

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

    @Override
    public <T> Optional<T> findMostGeneralMatch(@NotNull Map<? extends Class<?>, ? extends T> map, @NotNull Class<?> target) {
        Set<Class<?>> visited = new HashSet<>();
        return Optional.ofNullable(doFindMostGeneralMatch(map, target, visited));
    }

    private static <T> T doFindMostGeneralMatch(Map<? extends Class<?>, ? extends T> map, Class<?> type, Set<Class<?>> visited) {
        // Base case: if the type has already been visited, we return empty to avoid cycles
        if (type == null || !visited.add(type)) {
            return null;
        }

        // Check if the current class has a match
        T match = map.get(type);
        if (match != null) {
            // Continue looking up to see if there's a more general (ancestor) match
            T parentMatch = doFindMostGeneralMatch(map, type.getSuperclass(), visited);
            if (parentMatch != null) {
                return parentMatch;  // prefer more general ancestor match
            }

            // Check interfaces if no superclass match is found
            for (Class<?> interfaceType : type.getInterfaces()) {
                T interfaceMatch = doFindMostGeneralMatch(map, interfaceType, visited);
                if (interfaceMatch != null) {
                    return interfaceMatch;
                }
            }

            return match;
        }

        // Recursively check superclass
        T superClassMatch = doFindMostGeneralMatch(map, type.getSuperclass(), visited);
        if (superClassMatch != null) {
            return superClassMatch;
        }

        // Recursively check interfaces if no superclass match is found
        for (Class<?> interfaceType : type.getInterfaces()) {
            T interfaceMatch = doFindMostGeneralMatch(map, interfaceType, visited);
            if (interfaceMatch != null) {
                return interfaceMatch;
            }
        }

        // No match found in hierarchy
        return null;
    }
}
