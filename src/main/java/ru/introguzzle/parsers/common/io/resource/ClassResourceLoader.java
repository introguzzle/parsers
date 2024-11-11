package ru.introguzzle.parsers.common.io.resource;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import java.io.InputStream;
import java.nio.charset.Charset;

@AllArgsConstructor
public abstract class ClassResourceLoader<R> implements ResourceLoader<R> {
    @Override
    public @NotNull R load(@NotNull String path) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalArgumentException("Config file not found");
            }

            return getParser().parse(new String(in.readAllBytes(), Charset.defaultCharset()));
        } catch (Throwable e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }
}
