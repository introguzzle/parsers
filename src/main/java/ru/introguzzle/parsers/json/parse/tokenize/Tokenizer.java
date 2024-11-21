package ru.introguzzle.parsers.json.parse.tokenize;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Tokenizer {
    List<Token> tokenize(@NotNull String input);

    static Tokenizer newTokenizer() {
        return new TokenizerImpl();
    }
}
