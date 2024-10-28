package ru.introguzzle.parser.json.parse;

public class JSONTokenParserTest extends ParserTest {
    private final Parser parser = new JSONTokenParser();

    @Override
    public Parser getParser() {
        return parser;
    }
}
