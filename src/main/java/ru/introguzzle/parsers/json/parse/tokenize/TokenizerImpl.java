package ru.introguzzle.parsers.json.parse.tokenize;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.json.parse.JSONParseException;
import ru.introguzzle.parsers.common.utility.NumberUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TokenizerImpl implements Tokenizer {

    @Override
    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();

        boolean inQuotes;

        char[] charArray = input.toCharArray();
        int index = 0;
        while (index < charArray.length) {
            char c = charArray[index];
            buffer.append(c);

            if (Character.isWhitespace(c)) {
                index++;
                continue;
            }

            if (c == '"') {
                index++;
                inQuotes = true;
                while (inQuotes && index < charArray.length) {
                    c = charArray[index];
                    buffer.append(c);
                    if (c == '"') {
                        inQuotes = false;
                    }

                    index++;
                }

                tokens.add(new Token(buffer.toString(), Type.STRING));
                buffer.setLength(0);
                continue;
            }


            if (buffer.isEmpty()) {
                index++;
                continue;
            }

            Type type = getType(buffer.charAt(0), switch (buffer.toString()) {
                case "null"          -> Type.NULL;
                case "false", "true" -> Type.BOOLEAN;
                default              -> null;
            });

            if (type != null) {
                tokens.add(new Token(buffer, type));
                buffer.setLength(0);
            } else {
                String s = buffer.toString();
                int len = s.length();

                if (s.endsWith(",") || s.endsWith("}") || s.endsWith("]")) {
                    String value = s.substring(0, len - 1);
                    char delimiter = s.substring(len - 1).charAt(0);

                    tokens.add(new Token(value, Type.NUMBER));
                    tokens.add(new Token(delimiter, getType(delimiter, null)));

                    buffer.setLength(0);
                }
            }

            index++;
        }

        return validate(tokens);
    }

    private List<Token> validate(List<Token> tokens) {
        Stack<Token> stack = new Stack<>();

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            Type type = token.getType();
            String data = token.getData();

            Token next = null;

            if (i != tokens.size() - 1) {
                next = tokens.get(i + 1);
            }

            switch (type) {
                case OBJECT_START, ARRAY_START -> stack.push(token);
                case OBJECT_END -> {
                    if (stack.isEmpty()) {
                        throw getBracketException();
                    }

                    if (stack.peek().getType() != Type.OBJECT_START) {
                        throw getBracketException();
                    }

                    stack.pop();
                }
                case ARRAY_END -> {
                    if (stack.isEmpty()) {
                        throw getBracketException();
                    }

                    if (stack.peek().getType() != Type.ARRAY_START) {
                        throw getBracketException();
                    }

                    stack.pop();
                }
            }

            if (type == Type.STRING) {
                if (!data.startsWith("\"") || !data.endsWith("\"")) {
                    throw new JSONParseException("Invalid key: " + token.getData());
                }

                if (next != null && next.getType() == Type.COLON) {
                    token.setType(Type.KEY);
                }
            }

            if (next != null) {
                for (Type t: INVALID_SEQUENTIAL_TYPES) {
                    if (type == t && next.getType() == t) {
                        throw new JSONParseException("Invalid syntax");
                    }
                }
            }

            if (type == Type.NUMBER && !isNumeric(data)) {
                throw new JSONParseException("Invalid numeric value: " + data);
            }
        }

        if (!stack.isEmpty()) {
            throw getBracketException();
        }

        return tokens;
    }

    private static final Type[] INVALID_SEQUENTIAL_TYPES = {
            Type.COLON, Type.COMMA
    };

    private static boolean isNumeric(String string) {
        return NumberUtilities.isNumeric(string);
    }

    private static @NotNull JSONParseException getBracketException() {
        return new JSONParseException("Unmatched brackets");
    }

    private Type getType(char character, Type defaultType) {
        return switch (character) {
            case '{' -> Type.OBJECT_START;
            case '}' -> Type.OBJECT_END;
            case '[' -> Type.ARRAY_START;
            case ']' -> Type.ARRAY_END;
            case ':' -> Type.COLON;
            case ',' -> Type.COMMA;
            default  -> defaultType;
        };
    }
}
