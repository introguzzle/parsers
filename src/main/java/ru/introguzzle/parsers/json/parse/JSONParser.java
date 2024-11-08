package ru.introguzzle.parsers.json.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.JSONObject;

import java.util.*;

public class JSONParser extends Parser {
    private static final Map<Character, Character> BRACKET_MAP = Map.of(
            '{', '}',
            '[', ']',
            '(', ')'
    );

    private static final Set<Character> OPEN_BRACKETS = BRACKET_MAP.keySet();
    private static final Set<Character> CLOSE_BRACKETS = new HashSet<>(BRACKET_MAP.values());

    private static boolean isMatchingBracket(char open, char close) {
        return BRACKET_MAP.get(open) == close;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T parse(@Nullable String data, @NotNull Class<? extends T> type) {
        if (data == null) return null;
        validate(data);
        if (data.isEmpty()) {
            return handleEmptyString(data, type);
        }

        Object result;

        // If the string starts with '{', treat it as a JSON object
        if (data.startsWith("{")) {
            result = parseObject(data);
        }
        // If the string starts with '[', treat it as a JSON array
        else if (data.startsWith("[")) {
            result = parseArray(data);
        }
        // Otherwise, assume it is a primitive value (number, boolean, or string)
        else {
            result = handleEmptyString(data, type);
        }

        return (T) result;
    }

    @SuppressWarnings("unchecked")
    public <T> T map(String data, Class<? extends T> type) {
        Object result;

        // If the string starts with '{', treat it as a JSON object
        if (data.startsWith("{")) {
            result = parseObject(data);
        }
        // If the string starts with '[', treat it as a JSON array
        else if (data.startsWith("[")) {
            result = parseArray(data);
        }
        // Otherwise, assume it is a primitive value (number, boolean, or string)
        else {
            result = handlePrimitiveType(data, type);
        }

        return (T) result;
    }

    /**
     * Parses a JSON string representing an object and converts it into a {@code JSONObject}.
     *
     * @param data the raw JSON string representing an object
     * @return the parsed {@code JSONObject}
     */
    private JSONObject parseObject(String data) {
        JSONObject object = new JSONObject();
        // Remove the outer curly braces
        data = data.substring(1, data.length() - 1).trim();

        // Break down the JSON object into key-value pairs
        List<String> lines = getLines(data);
        int index = 0;
        for (String line : lines) {
            if (line.isEmpty() || line.isBlank()) {
                String problem = index > 1
                        ? lines.get(index - 1)
                        : lines.get(index);

                throw new JSONParseException("Invalid JSON syntax in line: " + problem);
            }

            // Split each line into a key-value pair by the first colon
            String[] split = line.split(":", 2);
            String key = split[0].trim().replace("\"", ""); // Remove double quotes from keys
            Object value = map(split[1].trim(), JSONObject.class); // Recursively map the value
            object.put(key, value);
            index++;
        }

        return object;
    }

    /**
     * Parses a JSON string representing an array and converts it into a {@code JSONArray}.
     *
     * @param data the raw JSON string representing an array
     * @return the parsed {@code JSONArray}
     */
    private JSONArray parseArray(String data) {
        JSONArray array = new JSONArray();
        // Remove the outer square brackets
        data = data.substring(1, data.length() - 1).trim();

        // Break down the array into individual elements
        List<String> elements = getLines(data);
        for (String element : elements) {
            array.add(map(element.trim(), JSONArray.class)); // Recursively map each element
        }

        return array;
    }

    /**
     * Splits a JSON object or array string into individual components or elements.
     * This method accounts for nested objects and arrays and ensures they are treated
     * as single entities.
     *
     * @param data the raw JSON string representing an object or array
     * @return a list of strings where each string is either a key-value pair (for objects)
     *         or an element (for arrays)
     */
    private List<String> getLines(String data) {
        List<String> entries = new ArrayList<>();
        int bracketCount = 0;
        boolean inQuotes = false;

        StringBuilder entry = new StringBuilder();

        // Iterate over each character in the string
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);

            // Toggle the inQuotes flag when encountering a double quote
            if (c == '"') {
                inQuotes = !inQuotes;
            }

            // If not inside a quoted string, track the opening and closing of brackets
            if (!inQuotes) {
                if (c == '{' || c == '[') {
                    bracketCount++;
                } else if (c == '}' || c == ']') {
                    bracketCount--;
                }

                // If we encounter a comma outside of nested objects/arrays, it's a delimiter
                if (c == ',' && bracketCount == 0) {
                    entries.add(entry.toString().trim());
                    entry.setLength(0); // Clear the entry for the next value
                    continue;
                }
            }

            // Append the current character to the entry being built
            entry.append(c);
        }

        // Add the final entry if there's anything left in the buffer
        if (!entry.isEmpty()) {
            entries.add(entry.toString().trim());
        }

        return entries;
    }

    public void validate(String data) {
        Stack<Character> stack = new Stack<>();

        boolean inQuotes = false;

        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            if (c == '"') inQuotes = !inQuotes;

            if (!inQuotes) for (char openBracket: OPEN_BRACKETS) {
                if (c == openBracket) {
                    stack.push(c);
                }
            }

            if (!inQuotes) for (char closeBracket: CLOSE_BRACKETS) {
                if (c != closeBracket) {
                    continue;
                }

                if (stack.isEmpty()) {
                    throw new JSONParseException("Invalid JSON: unmatched closing bracket at position " + i);
                }

                char openBracket = stack.pop();
                if (!isMatchingBracket(openBracket, c)) {
                    throw new JSONParseException("Invalid JSON: mismatched brackets at position " + i);
                }
            }
        }
    }
}
