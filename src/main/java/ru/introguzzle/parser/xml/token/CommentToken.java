package ru.introguzzle.parser.xml.token;

import ru.introguzzle.parser.xml.Type;

import java.io.Serial;

public non-sealed class CommentToken extends Token {
    @Serial
    private static final long serialVersionUID = 8848597924517164880L;

    public CommentToken(String data) {
        super(data, Type.COMMENT);
    }

    public String getContent() {
        return getData().substring(4, getData().length() - 4);
    }
}
