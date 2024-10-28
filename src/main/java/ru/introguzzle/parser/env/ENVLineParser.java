package ru.introguzzle.parser.env;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parser.common.line.Line;
import ru.introguzzle.parser.common.line.Line.Pair;

import java.util.List;
import java.util.Map;

public class ENVLineParser extends Parser {

    @Override
    protected @NotNull Map<String, String> parse(@NotNull String data,
                                                 @NotNull Map<String, String> env) {
        List<Line> lines = Line.stream(data, ENVLine::new)
                .map(line -> line.deletePrefix("export ").deleteComment())
                .toList();

        for (Line line : lines) {
            Pair pair = handleLine(line, env);
            if (pair != null && pair.value() != null) {
                env.put(pair.key().toString(), pair.value().toString());
            }
        }

        return env;
    }

    private @Nullable Pair handleLine(@NotNull Line line,
                                      @NotNull Map<String, String> env) {
        Pair pair = line.toPair();
        String key = pair.key().toString();

        if (pair.value() == null) {
            return null;
        }

        CharSequence value = new ENVLine(pair.value().toString())
                .unescape()
                .expand(env);

        return new Pair(key, value);
    }
}
