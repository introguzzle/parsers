package ru.introguzzle.parsers.json.parse.tokenize;

import java.util.List;

public interface Tokenizer {
    List<Token> tokenize(String input);
}
