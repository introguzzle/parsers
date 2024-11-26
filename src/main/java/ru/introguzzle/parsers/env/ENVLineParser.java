package ru.introguzzle.parsers.env;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.common.util.Line;
import ru.introguzzle.parsers.common.util.Line.Pair;

import java.util.List;

public class ENVLineParser extends Parser {

    @Override
    protected @NotNull Environment parse(@NotNull String data,
                                                 @NotNull Environment environment) {
        List<Line> lines = Line.stream(data, ENVLine::new)
                .map(line -> line.deletePrefix("export ").deleteComment())
                .toList();

        for (Line line : lines) {
            Pair pair = handleLine(line, environment);
            if (pair != null && pair.value() != null) {
                environment.put(pair.key().toString(), pair.value().toString());
            }
        }

        return environment;
    }

    private @Nullable Pair handleLine(@NotNull Line line,
                                      @NotNull Environment environment) {
        Pair pair = line.toPair();
        String key = pair.key().toString();

        if (pair.value() == null) {
            return null;
        }

        CharSequence value = new ENVLine(pair.value().toString())
                .unescape()
                .expand(environment);

        return new Pair(key, value);
    }
}
