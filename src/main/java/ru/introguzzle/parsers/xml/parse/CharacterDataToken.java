package ru.introguzzle.parsers.xml.parse;

import org.jetbrains.annotations.NotNull;

non-sealed class CharacterDataToken extends Token {
    public static final String HEAD = CharacterData.HEAD;
    public static final String TAIL = CharacterData.TAIL;

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
