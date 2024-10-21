package ru.introguzzle.parser.xml;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parser.xml.token.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class Tokenizer {
    public @NotNull List<Token> tokenize(@Nullable String data) {
        if (data == null) return new ArrayList<>();
        List<Token> tokens = new ArrayList<>();
        List<String> lines = split(data);

        tokens.add(getDeclarationToken(lines.getFirst()));
        Stack<ElementHeadToken> stack = new Stack<>();
        boolean seenRoot = false;

        for (int i = 1; i < lines.size(); i++) {
            Token token = handle(lines.get(i));

            if (token instanceof ElementHeadToken head) {
                if (!stack.isEmpty()) {
                    stack.peek().addChild(head);
                }

                stack.push(head);
                if (!seenRoot) {
                    seenRoot = true;
                    tokens.add(head);
                } else if (stack.isEmpty()) {
                    throw new IllegalStateException("More than one root found");
                }

            } else if (token instanceof ElementTailToken tail) {
                if (stack.isEmpty()) {
                    throw new IllegalStateException("No head match found for tail with name: " + tail.getName());
                }

                stack.pop();
            } else if (!stack.isEmpty()) {
                stack.peek().addChild(token);
            } else {
                tokens.add(token);
            }
        }

        if (!stack.isEmpty()) {
            throw new IllegalStateException("Unmatched tags");
        }

        return tokens;
    }

    private Token getDeclarationToken(String declaration) {
        List<AttributeToken> attributes = getAttributes(declaration);

        Version version = Version.of(attributes.get(0).getValue());
        Encoding encoding = Encoding.of(attributes.get(1).getValue());

        return new DeclarationToken(declaration, version, encoding);
    }

    private List<AttributeToken> getAttributes(String opening) {
        if (opening.isEmpty() || opening.isBlank()) {
            return new ArrayList<>();
        }

        String content = opening.substring(
                opening.indexOf(" ") + 1,
                Math.max(opening.lastIndexOf("'"), opening.lastIndexOf('"')) + 1
        );

        return Arrays.stream(content.split(" "))
                .map(s -> s.replace("\"", ""))
                .filter(s -> !s.isBlank() && !s.isEmpty())
                .map(AttributeToken::new)
                .toList();
    }


    private @NotNull Token handle(String line) {
        if (line.startsWith("<!--")) {
            return new CommentToken(line);
        }

        if (line.startsWith("<")) {
            if (line.startsWith("<![CDATA")) return new CDataToken(line);
            if (line.endsWith("/>")) {
                return new SelfClosingElementToken(getName(line),
                        line,
                        getAttributes(line));
            }

            if (line.startsWith("</")) {
                return new ElementTailToken(getName(line), line);
            }

            return new ElementHeadToken(getName(line),
                    line,
                    getAttributes(line)
            );
        }

        return new TextToken(line.strip());
    }

    private String getName(String line) {
        if (line.startsWith("</")) {
            return line.substring(2, line.indexOf(">"));
        }

        if (line.startsWith("<")) {
            int index = indexOf(line, " ", "/", ">");

            return line.substring(1, index);
        }

        return line;
    }

    private static int indexOf(String line, String... strings) {
        List<Integer> indices = Arrays.stream(strings)
                .map(line::indexOf)
                .toList();

        int min = indices
                .stream()
                .max(Integer::compareTo)
                .orElse(1);

        for (int index : indices) {
            if (index == -1) {
                continue;
            }

            min = Math.min(min, index);
        }

        return min;
    }

    private List<String> split(@NotNull String data) {
        return Arrays.stream(data.split("(?=<)|(?<=>)"))
                .filter(s -> !s.isBlank())
                .map(s -> s.replace("\n", "").stripLeading())
                .toList();
    }
}
