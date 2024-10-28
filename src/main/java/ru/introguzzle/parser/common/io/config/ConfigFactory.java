package ru.introguzzle.parser.common.io.config;

import ru.introguzzle.parser.yaml.Parser;
import ru.introguzzle.parser.yaml.YAMLDocument;
import ru.introguzzle.parser.yaml.SimpleYAMLParser;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;

public class ConfigFactory {
    public static final Parser PARSER = new SimpleYAMLParser(2);

    private static final class Holder {
        private static final ConfigFactory INSTANCE = new ConfigFactory();
    }

    public static ConfigFactory getInstance() {
        return Holder.INSTANCE;
    }

    private ConfigFactory() {

    }

    public YAMLDocument loadConfig(String path) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalArgumentException("Config file not found");
            }

            return PARSER.parse(new String(in.readAllBytes(), Charset.defaultCharset()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    public Optional<YAMLDocument> tryLoadConfig(String path) {
        try {
            return Optional.of(loadConfig(path));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
