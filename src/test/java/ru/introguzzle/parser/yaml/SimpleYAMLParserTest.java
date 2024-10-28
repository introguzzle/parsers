package ru.introguzzle.parser.yaml;

public class SimpleYAMLParserTest extends ParserTest {
    private final Parser parser = new SimpleYAMLParser(2);

    @Override
    public Parser getParser() {
        return parser;
    }
}
