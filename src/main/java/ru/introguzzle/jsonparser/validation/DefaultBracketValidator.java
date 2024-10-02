package ru.introguzzle.jsonparser.validation;

import ru.introguzzle.jsonparser.JSONParseException;

import java.util.Stack;

public class DefaultBracketValidator implements BracketValidator {
    @Override
    public Stack<Character> validate(String data) {
        Stack<Character> stack = new Stack<>();

        boolean inQuotes = false;

        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            if (c == '"') inQuotes = !inQuotes;

            for (char openBracket: OPEN_BRACKETS) {
                if (!inQuotes && c == openBracket) {
                    stack.push(c);
                }
            }

            for (char closeBracket: CLOSE_BRACKETS) {
                if (!inQuotes && c == closeBracket) {
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

        return stack;
    }
}
