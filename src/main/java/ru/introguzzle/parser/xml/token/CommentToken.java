package ru.introguzzle.parser.xml.token;

import ru.introguzzle.parser.xml.Type;

public non-sealed class CommentToken extends Token {
    public CommentToken(String data) {
        super(data, Type.COMMENT);
    }

    public String getContent() {
        return getData().substring(4, getData().length() - 4);
    }
}
