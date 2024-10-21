package ru.introguzzle.parser.xml.token;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parser.xml.Type;

public non-sealed class CDataToken extends Token {
    public CDataToken(@NotNull String data) {
        super(data, Type.CDATA);
    }
}
