package ru.introguzzle.parsers.common.io.resource;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.parse.BaseParser;
import ru.introguzzle.parsers.yaml.YAMLDocument;

import java.util.Optional;

/**
 * Interface for loading and parsing resource files into structured documents.
 * The {@code ResourceLoader} interface provides methods for loading resource data from specified
 * file paths and converting it into objects of type {@code R} using an associated parser.
 * It supports both mandatory loading, which throws exceptions on failure, and optional loading,
 * which gracefully handles errors by returning an empty result.
 *
 * @param <R> the type of the document resulting from parsing the resource (e.g., {@link YAMLDocument})
 */
public interface ResourceLoader<R> {

    /**
     * Retrieves the parser instance used for parsing resource files.
     * The parser is responsible for converting raw resource data into structured documents.
     *
     * @return a non-null {@link BaseParser} instance for parsing resources
     */
    @NotNull
    BaseParser<R> getParser();

    /**
     * Loads and parses a resource file from the specified path.
     * This method attempts to load the resource file located at the given path and
     * parses its contents into an object of type {@code R} using the associated parser.
     *
     * @param path the file system path to the resource file
     * @return a non-null instance of {@code R} representing the parsed resource
     * @throws RuntimeException if the resource file cannot be loaded or parsed successfully
     */
    @NotNull
    R load(@NotNull String path);

    /**
     * Attempts to load and parse a resource file from the specified path without throwing an exception.
     * This method tries to load and parse the resource file located at the given path.
     * If the loading or parsing process fails for any reason, it returns an empty {@code Optional}
     * instead of propagating the exception.
     *
     * @param path the file system path to the resource file
     * @return an {@code Optional} containing the parsed resource of type {@code R} if successful,
     *         or {@code Optional.empty()} if an error occurs during loading or parsing
     */
    default @NotNull Optional<R> tryLoad(@NotNull String path) {
        try {
            return Optional.of(load(path));
        } catch (Throwable e) {
            return Optional.empty();
        }
    }
}
