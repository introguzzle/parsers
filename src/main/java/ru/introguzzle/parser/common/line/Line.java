package ru.introguzzle.parser.common.line;

import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parser.common.Streams;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@ExtensionMethod(Streams.class)
public abstract class Line implements CharSequence, Serializable {
    @Serial
    private static final long serialVersionUID = -8752243807650064896L;

    public static final String COMMENT_SPLIT_STRING = "#";
    public static final Character COMMENT_SPLIT_CHAR = '#';

    public record Pair(@NotNull CharSequence key, @Nullable CharSequence value) {}

    private final String content;

    public Line(String content) {
        this.content = content;
    }

    public abstract Supplier<? extends Line> getSupplier(String content);

    @Override
    public boolean isEmpty() {
        return content.isEmpty() || content.isBlank();
    }

    public boolean contains(CharSequence sequence) {
        return content.contains(sequence);
    }

    @NotNull
    @Override
    public Line subSequence(int start, int end) {
        return subLine(start, end);
    }

    public boolean isComment() {
        return startsWith(COMMENT_SPLIT_STRING);
    }

    public Line unescape() {
        String newContent = null;
        if (content.startsWith("\"") && content.endsWith("\"")) {
            newContent = content.substring(1, content.length() - 1)
                    .replace("\\\"", "\"")
                    .replace("\\n", "\n")
                    .replace("\\t", "\t")
                    .replace("\\\\", "\\");

        } else if (content.startsWith("'") && content.endsWith("'")) {
            newContent = content.substring(1, content.length() - 1);
        }

        return newContent == null ? this : getSupplier(newContent).get();
    }

    public Line expand(@NotNull Map<String, String> map) {
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)}");
        Matcher matcher = pattern.matcher(this);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String variable = matcher.group(1);
            String variableValue = map.getOrDefault(variable, System.getenv(variable));
            if (variableValue != null) {
                matcher.appendReplacement(builder, Matcher.quoteReplacement(variableValue));
            }
        }

        matcher.appendTail(builder);
        return getSupplier(builder.toString()).get();
    }

    @Override
    public int length() {
        return content.length();
    }

    @Override
    public char charAt(int index) {
        return content.charAt(index);
    }

    public Line deletePrefix(@NotNull String prefix) {
        if (prefix.isEmpty()) {
            return this;
        }

        return startsWith(prefix)
                ? getSupplier(content.substring(prefix.length())).get()
                : this;
    }

    public boolean startsWith(String prefix) {
        return content.startsWith(prefix);
    }

    public boolean endsWith(String suffix) {
        return content.endsWith(suffix);
    }

    public Line deleteComment() {
        int index = -1;

        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        int length = length();
        for (int i = 0; i < length; i++) {
            char c = charAt(i);
            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (c == '\"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            } else if (c == COMMENT_SPLIT_CHAR && !inSingleQuote && !inDoubleQuote) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            return subLine(0, index);
        }

        return this;
    }

    public Line subLine(int from, int to) {
        return getSupplier(content.substring(from, to)).get();
    }

    public abstract Pair toPair();

    public String[] split(String regex) {
        return content.split(regex);
    }

    public String[] split(String regex, int limit) {
        return content.split(regex, limit);
    }

    public static <T extends Line> Stream<T> stream(String data, Function<? super String, T> mapper) {
        return data.lines()
                .map(mapper)
                .reject(Line::isComment)
                .reject(Line::isEmpty);
    }

    @Override
    public @NotNull String toString() {
        return content;
    }
}
