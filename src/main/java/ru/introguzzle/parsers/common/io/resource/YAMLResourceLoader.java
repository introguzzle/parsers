package ru.introguzzle.parsers.common.io.resource;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.parse.BaseParser;
import ru.introguzzle.parsers.yaml.Parser;
import ru.introguzzle.parsers.yaml.YAMLDocument;
import ru.introguzzle.parsers.yaml.SimpleYAMLParser;

import java.io.InputStream;
import java.nio.charset.Charset;

public class YAMLResourceLoader implements ResourceLoader<YAMLDocument> {
    public static final Parser PARSER = new SimpleYAMLParser(2);

    private static final class Holder {
        private static final ResourceLoader<YAMLDocument> INSTANCE = new YAMLResourceLoader();
    }

    public static ResourceLoader<YAMLDocument> getLoader() {
        return Holder.INSTANCE;
    }

    private YAMLResourceLoader() {

    }

    @Override
    public @NotNull BaseParser<YAMLDocument> getParser() {
        return PARSER;
    }

    @Override
    public @NotNull YAMLDocument load(String path) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalArgumentException("Config file not found");
            }

            return PARSER.parse(new String(in.readAllBytes(), Charset.defaultCharset()));
        } catch (Throwable e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }
}
