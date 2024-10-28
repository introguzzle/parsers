package ru.introguzzle.parser.env;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parser.common.line.Line;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ENVParser extends Parser {
    private record Pair(String key, String value) {}

    @Override
    protected @NotNull Map<String, String> parse(@NotNull String data,
                                                 @NotNull Map<String, String> env) {
        String[] lines = data.split(System.lineSeparator());
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.isBlank() || line.startsWith("#")) {
                continue;
            }

            if (line.startsWith("export ")) {
                line = line.substring(7).trim();
            }

            int hashIndex = -1;
            boolean inSingleQuote = false;
            boolean inDoubleQuote = false;
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '\'' && !inDoubleQuote) {
                    inSingleQuote = !inSingleQuote;
                } else if (c == '\"' && !inSingleQuote) {
                    inDoubleQuote = !inDoubleQuote;
                } else if (c == '#' && !inSingleQuote && !inDoubleQuote) {
                    hashIndex = i;
                    break;
                }
            }
            if (hashIndex != -1) {
                line = line.substring(0, hashIndex).trim();
            }

            Pair pair = handleLine(line, env);
            if (pair != null) {
                env.put(pair.key, pair.value);
            }
        }

        return env;
    }

    private @Nullable Pair handleLine(@NotNull String line,
                                      @NotNull Map<String, String> env) {
        String[] tokens = line.split("=", 2);

        if (tokens.length != 2) {
            return null;
        }

        String key = tokens[0];
        String value = tokens[1];

        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1)
                    .replace("\\\"", "\"")
                    .replace("\\n", "\n")
                    .replace("\\t", "\t")
                    .replace("\\\\", "\\");

            value = new ENVLine(value).expand(env).toString();

        } else if (value.startsWith("'") && value.endsWith("'")) {
            value = value.substring(1, value.length() - 1);
        }

        return new Pair(key, value);
    }

    public static void main(String[] args) throws IOException {
        try (InputStream in = ENVParser.class.getClassLoader().getResourceAsStream("ru/introguzzle/parser/config/.env")) {
            if (in != null) {
                String data = new String(in.readAllBytes());
                ENVParser parser1 = new ENVParser();
                ENVLineParser parser2 = new ENVLineParser();

                Map<String, String> env1 = parser1.parse(data);
                Map<String, String> env2 = parser2.parse(data);

                System.out.println();
            }
        }
    }
}
