package ru.introguzzle.parsers.common.io.resource;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.parse.BaseParser;
import ru.introguzzle.parsers.yaml.YAMLDocument;

import java.util.Optional;

/**
 * Interface for loading configuration files and parsing them into YAML documents.
 * The {@code ConfigLoader} interface provides methods for loading configuration
 * data from specified file paths and converting it to {@code YAMLDocument} objects.
 * It includes functionality for both guaranteed and optional loading with error handling.
 */
public interface ResourceLoader<T> {

    /**
     * Retrieves the {@code Parser} instance used for parsing YAML configuration files.
     *
     * @return the parser instance, which must not be null.
     */
    @NotNull
    BaseParser<YAMLDocument> getParser();

    /**
     * Loads and parses a configuration file from the given path.
     * This method attempts to load the configuration file from the specified path
     * and convert it into a {@code YAMLDocument} using the provided parser.
     *
     * @param path the path to the configuration file.
     * @return a {@code YAMLDocument} representing the parsed configuration.
     * @throws RuntimeException if the configuration file could not be loaded or parsed.
     */
    @NotNull
    T load(String path);

    /**
     * Attempts to load a configuration file from the given path without throwing an exception.
     * This method tries to load and parse the configuration file from the specified path.
     * If loading or parsing fails, it returns an empty {@code Optional} instead of throwing an exception.
     *
     * @param path the path to the configuration file.
     * @return an {@code Optional} containing the {@code YAMLDocument} if the file was successfully loaded,
     *         or an empty {@code Optional} if an error occurred.
     */
    default @NotNull Optional<T> tryLoad(String path) {
        try {
            return Optional.of(load(path));
        } catch (Throwable e) {
            return Optional.empty();
        }
    }
}
