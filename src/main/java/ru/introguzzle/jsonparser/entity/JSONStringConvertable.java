package ru.introguzzle.jsonparser.entity;

import ru.introguzzle.jsonparser.mapping.context.CircularReferenceStrategy;

import java.util.Iterator;
import java.util.Map;

public interface JSONStringConvertable {
    String TAB = "    ";

    int INITIAL_LEVEL = 1;

    Iterator<?> getIterator();
    String getOpeningSymbol();
    String getClosingSymbol();
    int size();

    default String toJSONString() {
        return toJSONStringRecursive(INITIAL_LEVEL, false);
    }

    default String toJSONStringCompact() {
        return toJSONStringRecursive(INITIAL_LEVEL, true);
    }

    private String toJSONStringRecursive(int level, boolean compact) {
        String newLine = compact ? "" : System.lineSeparator();

        StringBuilder result = new StringBuilder();
        if (level == INITIAL_LEVEL) {
            result.append(getOpeningSymbol()).append(newLine);
        }

        int i = 0;
        Iterator<?> iterator = getIterator();
        while (iterator.hasNext()) {
            Object value = iterator.next();
            result.append(TAB.repeat(level));

            if (value instanceof Map.Entry<?, ?> entry) {
                result.append("\"").append(entry.getKey()).append("\"").append(": ");
                value = entry.getValue();
            }

            if (value instanceof JSONStringConvertable convertable) {
                if (convertable.size() == 0) {
                    result.append(convertable.getOpeningSymbol())
                            .append(convertable.getClosingSymbol());
                } else {
                    result.append(convertable.getOpeningSymbol()).append(newLine)
                            .append(convertable.toJSONStringRecursive(level + 1, compact))
                            .append(TAB.repeat(level))
                            .append(convertable.getClosingSymbol());
                }
            } else {
                if (value instanceof String s
                        && s.charAt(0) != '\"'
                        && s.charAt(s.length() - 1) != '\"'
                        && !s.contentEquals(CircularReferenceStrategy.PLACEHOLDER)) {

                    result.append("\"")
                            .append(s)
                            .append("\"");
                } else {
                    result.append(value);
                }
            }

            if (i != size() - 1) {
                result.append(",");
            }

            result.append(newLine);
            i++;
        }

        return level == INITIAL_LEVEL
                ? result.append(getClosingSymbol()).toString()
                : result.toString();
    }
}
