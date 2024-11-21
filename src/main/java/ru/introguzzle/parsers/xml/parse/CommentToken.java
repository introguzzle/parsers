package ru.introguzzle.parsers.xml.parse;

import java.io.Serial;

non-sealed class CommentToken extends Token {
    @Serial
    private static final long serialVersionUID = 8848597924517164880L;

    public CommentToken(String data) {
        super(data, Type.COMMENT);
    }

    public String getContent() {
        return getData().substring(4, getData().length() - 4);
    }
}
