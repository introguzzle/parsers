package ru.introguzzle.parsers.env;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.io.IO;
import ru.introguzzle.parsers.common.util.Line;
import ru.introguzzle.parsers.common.parse.BaseParser;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Supplier;

public abstract class Parser implements BaseParser<Environment> {
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

    protected abstract @NotNull Environment parse(@NotNull String data,
                                                  @NotNull Environment environment);

    @Override
    public final @NotNull Environment parse(@NotNull String data) {
        Environment environment = parse(data, new Environment());
        return parse(data, environment);
    }

    public final @NotNull Environment parse(@NotNull Path path,
                                            @NotNull Charset encoding) {
        return parse(IO.readString(path, encoding));
    }

    public final @NotNull Environment parseDefault() {
        return parse(IO.readString(
                ".env",
                System.getProperty("user.dir"),
                IO.getRoot().orElse(null),
                System.getProperty("env.file"),
                System.getenv("ENV_FILE")
        ));
    }
}
