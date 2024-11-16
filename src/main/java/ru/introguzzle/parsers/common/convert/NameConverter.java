package ru.introguzzle.parsers.common.convert;

import java.util.function.Function;

/**
 * A functional interface that represents a conversion function for strings, typically used for converting names.
 * <p>
 * This interface extends {@link Function}, meaning it can be used wherever a function that takes a {@code String}
 * and returns a transformed {@code String} is required.
 * </p>
 * <p>
 * It is commonly used for tasks such as converting field names between different naming conventions (e.g., camelCase
 * to snake_case, or PascalCase to lowercase_with_underscores).
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 * NameConverter camelToSnake = name -> name.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
 * String convertedName = camelToSnake.apply("exampleName");
 * System.out.println(convertedName); // Outputs: example_name
 * </pre>
 * </p>
 *
 * @see Function
 */
@FunctionalInterface
public interface NameConverter extends Function<String, String> {

}
