package ru.introguzzle.parser.common.io;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.*;

/**
 * Utility class providing simplified I/O operations.
 * <p>
 * The {@code IO} class offers static methods that wrap around {@link Files} methods,
 * rethrowing {@link IOException} as {@link WrappedIOException} for unchecked exception handling.
 * This class also includes additional utility methods for common file operations.
 * </p>
 */
@UtilityClass
public final class IO {
    /**
     * Retrieves the last modified time of a file.
     *
     * @param path the path to the file
     * @return the {@link FileTime} representing the last modified time
     * @throws WrappedIOException if an I/O error occurs
     * @see Files#getLastModifiedTime(Path, java.nio.file.LinkOption...)
     */
    public static FileTime getLastModifiedTime(Path path) {
        try {
            return Files.getLastModifiedTime(path);
        } catch (IOException e) {
            throw new WrappedIOException(e);
        }
    }

    /**
     * Reads the content of a file as a {@code String} using the default charset.
     *
     * @param path the path to the file
     * @return the content of the file as a {@code String}
     * @throws WrappedIOException if an I/O error occurs
     * @see Files#readString(Path)
     */
    public static @NotNull String readString(@NotNull Path path) {
        return readString(path, Charset.defaultCharset());
    }

    /**
     * Reads all bytes from a file.
     *
     * @param path the path to the file
     * @return a byte array containing the contents of the file
     * @throws WrappedIOException if an I/O error occurs
     * @see Files#readAllBytes(Path)
     */
    public static byte[] readAllBytes(@NotNull Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new WrappedIOException(e);
        }
    }

    /**
     * Reads the content of a file as a {@code String} using the specified charset.
     *
     * @param path     the path to the file
     * @param encoding the charset to use for decoding the file's content
     * @return the content of the file as a {@code String}
     * @throws WrappedIOException if an I/O error occurs
     * @see Files#readString(Path, Charset)
     */
    public static @NotNull String readString(@NotNull Path path, @NotNull Charset encoding) {
        try {
            return Files.readString(path, encoding);
        } catch (IOException e) {
            throw new WrappedIOException(e);
        }
    }

    /**
     * Tests whether a file exists.
     *
     * @param path the path to the file to test
     * @return {@code true} if the file exists; {@code false} if the file does not exist
     * or its existence cannot be determined
     * @see Files#exists(Path, java.nio.file.LinkOption...)
     */
    public static boolean exists(@NotNull Path path) {
        return Files.exists(path);
    }

    /**
     * Retrieves the root directory of the project.
     *
     * @return an {@link Optional} containing the project root directory as a {@code String},
     * or an empty {@code Optional} if not available
     */
    public static @NotNull Optional<String> getRoot() {
        return Optional.ofNullable(Directory.getRoot());
    }

    /**
     * Reads the content of a file with the given name from the specified directories.
     * <p>
     * Searches for the file in the provided directories and returns its content as a {@code String}.
     * If the file is not found in any of the directories, throws a {@link WrappedIOException}.
     * </p>
     *
     * @param name        the name of the file to search for
     * @param directory   the primary directory to search in (can be {@code null})
     * @param directories additional directories to search in (can be {@code null})
     * @return the content of the file as a {@code String}
     * @throws WrappedIOException if the file is not found in any of the specified directories
     */
    public static @NotNull String readString(@NotNull String name,
                                             @Nullable String directory,
                                             @Nullable String... directories) {
        List<Path> paths = new ArrayList<>();
        if (directory != null) {
            paths.add(Path.of(directory, name));
        }

        if (directories != null) {
            paths.addAll(Arrays.stream(directories)
                    .filter(Objects::nonNull)
                    .map(dir -> Path.of(dir, name))
                    .toList());
        }

        for (Path path : paths) {
            if (exists(path)) {
                return readString(path);
            }
        }

        StringBuilder message = new StringBuilder("Failed to locate file with name '" + name + "' in the following paths:\n");
        int index = 1;
        for (Path path : paths) {
            message.append(index).append(") ").append(path).append("\n");
            index++;
        }

        throw new WrappedIOException(message.toString(), new FileNotFoundException());
    }
}
