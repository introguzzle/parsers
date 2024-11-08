package ru.introguzzle.parsers.env;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ENVParser extends Parser {
    private record Pair(String key, String value) {}

    @Override
    protected @NotNull Environment parse(@NotNull String data,
                                         @NotNull Environment environment) {
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

            Pair pair = handleLine(line, environment);
            if (pair != null) {
                environment.put(pair.key, pair.value);
            }
        }

        return environment;
    }

    private @Nullable Pair handleLine(@NotNull String line,
                                      @NotNull Environment environment) {
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

            value = new ENVLine(value).expand(environment).toString();

        } else if (value.startsWith("'") && value.endsWith("'")) {
            value = value.substring(1, value.length() - 1);
        }

        return new Pair(key, value);
    }
}
