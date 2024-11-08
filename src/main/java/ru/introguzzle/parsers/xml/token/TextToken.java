package ru.introguzzle.parsers.xml.token;

import java.io.Serial;

public non-sealed class TextToken extends Token {
    @Serial
    private static final long serialVersionUID = 9103245221931499913L;

    public TextToken(String data) {
        super(data, Type.TEXT);
    }
}
