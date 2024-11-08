package ru.introguzzle.parsers.yaml;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.line.Line;

import java.util.function.Supplier;

public abstract class YAMLParser implements Parser {
    private final int spacesCount;

    public YAMLParser(int spacesCount) {
        if (spacesCount != 2 && spacesCount != 4) {
            throw new IllegalArgumentException("Spaces count must be 2 or 4");
        }

        this.spacesCount = spacesCount;
    }

    @Override
    public abstract @NotNull YAMLDocument parse(@NotNull String data);

    @Getter
    final class YAMLLine extends Line {
        final int level;

        YAMLLine(String content, int level) {
            super(content);
            this.level = level;
        }

        @Override
        public Supplier<? extends Line> getSupplier(String content) {
            return () -> new YAMLLine(content, level);
        }

        @Override
        public YAMLLine deleteComment() {
            String string = super.deleteComment().toString();
            return new YAMLLine(string.strip().trim(), countLevel(this));
        }

        @Override
        public Pair toPair() {
            Line unescaped = unescape();
            String[] split = unescaped.split(":", 2);
            String key = split[0];

            if (split.length == 2) {
                return split[1].isEmpty()
                        ? new Pair(key, null)
                        : new Pair(key, split[1].stripLeading());
            }

            return new Pair(key, null);
        }

        YAMLLine strip() {
            return new YAMLLine(toString().strip().trim(), level);
        }

        boolean isSimpleEntry() {
            Pair pair = toPair();
            CharSequence value = pair.value();
            return value != null && !value.isEmpty();
        }
    }

    protected int countLevel(CharSequence line) {
        int level = 0;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (Character.isWhitespace(c)) {
                level++;
            } else {
                return level / spacesCount;
            }
        }

        return level / spacesCount;
    }
}
