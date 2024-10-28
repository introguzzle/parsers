package ru.introguzzle.parser.env;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parser.common.io.IO;
import ru.introguzzle.parser.common.line.Line;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class Parser {
    static final class ENVLine extends Line {
        public ENVLine(String content) {
            super(content);
        }

        @Override
        public Supplier<ENVLine> getSupplier(String content) {
            return () -> new ENVLine(content.trim().strip());
        }

        @Override
        public Pair toPair() {
            String[] split = split("=", 2);
            if (split.length == 2) {
                return new Pair(split[0], split[1]);
            }

            return new Pair(split[0], null);
        }
    }

    protected abstract @NotNull Map<String, String> parse(@NotNull String data,
                                                          @NotNull Map<String, String> env);

    public final @NotNull Map<String, String> parse(@NotNull String data) {
        Map<String, String> env = parse(data, new LinkedHashMap<>());
        return parse(data, env);
    }

    public final @NotNull Map<String, String> parse(@NotNull Path path,
                                                    @NotNull Charset encoding) {
        return parse(IO.readString(path, encoding));
    }

    public final @NotNull Map<String, String> parseDefault() {
        return parse(IO.readString(
                ".env",
                System.getProperty("user.dir"),
                IO.getRoot().orElse(null),
                System.getProperty("env.file"),
                System.getenv("ENV_FILE")
        ));
    }
}
