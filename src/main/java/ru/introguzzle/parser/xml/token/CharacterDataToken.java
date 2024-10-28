package ru.introguzzle.parser.xml.token;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parser.xml.Type;

public non-sealed class CharacterDataToken extends Token {
    public static final String HEAD = "<![CDATA[";
    public static final String TAIL = "]]>";

    public CharacterDataToken(@NotNull String data) {
        super(data, Type.CHARACTER_DATA);
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
