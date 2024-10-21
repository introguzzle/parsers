package ru.introguzzle.parser.xml.token;

import ru.introguzzle.parser.xml.Type;

public non-sealed class TextToken extends Token {
    public TextToken(String data) {
        super(data, Type.TEXT);
    }
}
