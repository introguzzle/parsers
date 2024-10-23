package ru.introguzzle.parser.xml.token;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parser.xml.Type;

public non-sealed class CDataToken extends Token {
    public static final String HEAD = "<![CDATA[";
    public static final String TAIL = "]]>";

    public CDataToken(@NotNull String data) {
        super(data, Type.CDATA);
    }

    public String getText() {
        String data = getData();
        if (data.startsWith(HEAD) && data.endsWith(TAIL)) {
            return data.substring(HEAD.length(), data.length() - TAIL.length());
        } else {
            throw new IllegalStateException("Invalid CDATA format");
        }
    }
}
