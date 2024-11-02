package ru.introguzzle.parser.common.utilities;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class NamingUtilities {
    /**
     * Converts a given string to snake_case format.
     * <p>
     * This method transforms each uppercase letter in the input string to lowercase and
     * precedes it with an underscore ('_') if it's not the first character.
     * For example, "camelCase" or "TitleCase" becomes "camel_case".
     * </p>
     *
     * @param input the string to be converted to snake_case
     * @return the snake_case version of the input string, or the original input if it's null or empty
     */
    public static String toSnakeCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();

        char[] characters = input.toCharArray();
        char first = characters[0];
        if (Character.isUpperCase(first)) {
            first = Character.toLowerCase(first);
        }

        result.append(first);

        if (characters.length < 2) {
            return result.toString();
        }

        for (int i = 1; i < characters.length; i++) {
            char c = characters[i];

            if (Character.isDigit(c)) {
                if (!result.isEmpty() && !containsOnlyDigits(result)) {
                    result.append("_");
                }

                while (i < characters.length && Character.isDigit(characters[i])) {
                    result.append(characters[i]);
                    i++;
                }

                if (i != characters.length) {
                    result.append("_");
                    result.append(Character.toLowerCase(characters[i]));
                }

                continue;
            }

            if (Character.isUpperCase(c) && !result.isEmpty()) {
                if (characters[i - 1] != '_') {
                    result.append('_'); // Append an underscore before uppercase letters
                }
            }

            result.append(Character.toLowerCase(c)); // Append the lowercase version of the character
        }

        return result.toString();
    }

    private static boolean containsOnlyDigits(CharSequence input) {
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Converts a given string from snake_case to camelCase format.
     * <p>
     * This method capitalizes the letter following each underscore ('_') and removes the underscores.
     * The first letter of the resulting string will be in lowercase.
     * For example, "snake_case" becomes "snakeCase".
     * </p>
     *
     * @param input the string to be converted to camelCase
     * @return the camelCase version of the input string, or the original input if it's null or empty
     */
    public static String toCamelCase(final String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        boolean upperCaseNext = false; // Flag to indicate if the next character should be uppercase

        for (char c : input.toCharArray()) {
            if (c == '_') {
                upperCaseNext = true; // Set the flag when an underscore is encountered
            } else {
                if (upperCaseNext) {
                    result.append(Character.toUpperCase(c)); // Capitalize the current character
                    upperCaseNext = false; // Reset the flag
                } else {
                    result.append(Character.toLowerCase(c)); // Append the lowercase version
                }
            }
        }

        return result.toString();
    }
}
