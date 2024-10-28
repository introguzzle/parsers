package ru.introguzzle.parser.json.parse;

public class JSONParserTest extends ParserTest {
    private final JSONParser parser = new JSONParser();

    @Override
    public Parser getParser() {
        return parser;
    }
}
