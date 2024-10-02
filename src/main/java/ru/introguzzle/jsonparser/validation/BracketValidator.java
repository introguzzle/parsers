package ru.introguzzle.jsonparser.validation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public interface BracketValidator {
    Map<Character, Character> BRACKET_MAP = Map.of(
            '{', '}',
            '[', ']',
            '(', ')'
    );

    Set<Character> OPEN_BRACKETS = BRACKET_MAP.keySet();
    Set<Character> CLOSE_BRACKETS = new HashSet<>(BRACKET_MAP.values());

    Stack<Character> validate(String data);

    default boolean isMatchingBracket(char open, char close) {
        return BRACKET_MAP.get(open) == close;
    }
}
