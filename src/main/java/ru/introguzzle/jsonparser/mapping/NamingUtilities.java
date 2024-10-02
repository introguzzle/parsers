package ru.introguzzle.jsonparser.mapping;

public class NamingUtilities {
    public static String toSnakeCase(final String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (Character.isUpperCase(c) && !result.isEmpty()) {
                result.append('_');
            }
            result.append(Character.toLowerCase(c));
        }

        return result.toString();
    }

    public static String toCamelCase(final String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        boolean upperCaseNext = false;

        for (char c : input.toCharArray()) {
            if (c == '_') {
                upperCaseNext = true;
            } else {
                if (upperCaseNext) {
                    result.append(Character.toUpperCase(c));
                    upperCaseNext = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }

        return result.toString();
    }
}
