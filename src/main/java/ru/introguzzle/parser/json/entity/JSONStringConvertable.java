package ru.introguzzle.parser.json.entity;

import ru.introguzzle.parser.json.mapping.reference.StandardCircularReferenceStrategies;

import java.util.Iterator;
import java.util.Map;

/**
 * Interface representing an entity that can be converted to a JSON string.
 * <p>
 * This interface defines methods to retrieve the opening and closing symbols,
 * iterate over its contents, and obtain the size of the entity. It provides
 * default methods for converting the entity to a JSON string in both compact
 * and pretty-printed formats.
 * </p>
 */
public interface JSONStringConvertable {

    /** A string representing a tab for indentation. */
    String TAB = "\t";

    /** The initial indentation level for JSON serialization. */
    int INITIAL_LEVEL = 1;
    String QUOTE = "\"";

    /**
     * Returns an iterator for the contents of this entity.
     *
     * @return an iterator over the contents
     */
    Iterator<?> getIterator();

    /**
     * Returns the opening symbol (e.g., '{' for a JSON object or '[' for a JSON array).
     *
     * @return the opening symbol
     */
    String getOpeningSymbol();

    /**
     * Returns the closing symbol (e.g., '}' for a JSON object or ']' for a JSON array).
     *
     * @return the closing symbol
     */
    String getClosingSymbol();

    /**
     * Returns the number of elements in this entity.
     *
     * @return the size of the entity
     */
    int size();

    /**
     * Converts this entity to a JSON string in a pretty-printed format.
     *
     * @return a pretty-printed JSON string representation of this entity
     */
    default String toJSONString() {
        return toJSONStringRecursive(INITIAL_LEVEL, false);
    }

    /**
     * Converts this entity to a JSON string in a compact format.
     *
     * @return a compact JSON string representation of this entity
     */
    default String toJSONStringCompact() {
        return toJSONStringRecursive(INITIAL_LEVEL, true);
    }

    /**
     * Recursively converts this entity to a JSON string.
     *
     * @param level  the current indentation level
     * @param compact whether to produce a compact JSON string
     * @return a JSON string representation of this entity
     */
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
                result.append(QUOTE).append(entry.getKey()).append(QUOTE).append(": ");
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
                        && !s.contentEquals(StandardCircularReferenceStrategies.PLACEHOLDER)) {

                    result.append(QUOTE)
                            .append(s)
                            .append(QUOTE);
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
