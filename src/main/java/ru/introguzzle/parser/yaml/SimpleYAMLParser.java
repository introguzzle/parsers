package ru.introguzzle.parser.yaml;

import ru.introguzzle.parser.common.line.Line;
import ru.introguzzle.parser.common.line.Line.Pair;

import java.util.List;
import java.util.Stack;

public class SimpleYAMLParser extends YAMLParser {
    public SimpleYAMLParser(int spacesCount) {
        super(spacesCount);
    }

    @Override
    public YAMLDocument parse(String data) {
        YAMLDocument root = new YAMLDocument();
        Stack<YAMLDocument> stack = new Stack<>();
        stack.push(root);

        List<YAMLLine> lines = Line.stream(data, s -> new YAMLLine(s, countLevel(s)))
                .map(YAMLLine::deleteComment)
                .map(YAMLLine::strip)
                .toList();

        int currentLevel = 0;

        for (YAMLLine line : lines) {
            if (line.contains("\t")) {
                throw new YAMLParseException("Tabs are not allowed");
            }

            int level = line.level;
            Pair pair = line.toPair();
            String key = pair.key().toString();

            if (level > currentLevel) {
                if (level - currentLevel != 1) {
                    throw new YAMLParseException("Invalid indentation at line: " + line);
                }

                currentLevel = level;
            } else if (level < currentLevel) {
                while (level < currentLevel) {
                    stack.pop();
                    currentLevel--;
                }
            }

            YAMLDocument currentDocument = stack.peek();

            if (line.isSimpleEntry()) {
                assert pair.value() != null;
                currentDocument.put(key, pair.value().toString().replace("\"", ""));
            } else {
                YAMLDocument inner = new YAMLDocument();
                currentDocument.put(key, inner);
                stack.push(inner);
            }
        }

        return root;
    }
}
