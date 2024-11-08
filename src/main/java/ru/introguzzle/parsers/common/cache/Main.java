package ru.introguzzle.parsers.common.cache;

import lombok.experimental.ExtensionMethod;
import ru.introguzzle.parsers.common.Streams;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@ExtensionMethod(Streams.class)
public class Main {

    /**
     * Record to hold file path and its content.
     */
    record Content(Path path, String content) {
    }

    /**
     * Record to hold line number and nesting level.
     * Note: This record is not used in the current implementation but can be useful for future extensions.
     */
    record Count(int line, int nestingLevel) {
    }

    /**
     * Record to hold the result containing file path, line number, and nesting level.
     */
    record Result(Path path, int line, int nestingLevel) {
    }

    /**
     * Counts the nesting level based on leading spaces.
     *
     * @param s the string to analyze
     * @return the nesting level
     */
    public static int countNestingLevel(String s) {
        int level = 0;
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == ' ') {
                level++;
            } else if (c == '\t') {
                level += 4; // Assuming a tab equals 4 spaces
            } else {
                break;
            }
            i++;
        }
        // Normalize the nesting level based on indentation unit
        int indentationUnit = 4; // Define your indentation unit here
        return level / indentationUnit;
    }

    private static void test() {

    }

    private static void extracted() throws Throwable {
        String src = "C:\\Users\\666\\IdeaProjects\\parsers\\src";
        try (Stream<Path> stream = Files.walk(Path.of(src), Integer.MAX_VALUE)) {
            List<Result> list = stream.filter(Files::isRegularFile)
                    .mapThrowing(path -> new Content(path, Files.readString(path)))
                    .flatMap(content -> {
                        List<String> lines = content.content.lines().toList();
                        return IntStream.range(0, lines.size())
                                .mapToObj(i -> new AbstractMap.SimpleImmutableEntry<>(i, countNestingLevel(lines.get(i))))
                                .max(Comparator.comparingInt(Map.Entry::getValue))
                                .map(entry -> new Result(content.path, entry.getKey(), entry.getValue()))
                                .stream(); // Converts Optional<Result> to Stream<Result>
                    })
                    .sorted(Comparator.comparingInt(Result::nestingLevel))
                    .toList();

            list.forEach(System.out::println);
        }
    }
}
