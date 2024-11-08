package ru.introguzzle.parsers.json.parse;

import ru.introguzzle.parsers.json.parse.tokenize.Token;

import java.util.List;

public final class TokenBuffer {
    int position;
    final List<Token> tokens;

    public TokenBuffer(List<Token> tokens) {
        this.position = 0;
        this.tokens = tokens;
    }

    public Token current() {
        return tokens.get(position);
    }

    public Token next() {
        position++;
        return current();
    }

    public int size() {
        return tokens.size();
    }
}
