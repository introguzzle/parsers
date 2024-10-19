package ru.introguzzle.jsonparser.parse.tokenize;

import java.util.List;

public interface Tokenizer {
    List<Token> tokenize(String input);
}
