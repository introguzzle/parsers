package ru.introguzzle.parser.xml.token;

import ru.introguzzle.parser.xml.Encoding;
import ru.introguzzle.parser.xml.Type;
import ru.introguzzle.parser.xml.Version;

public non-sealed class DeclarationToken extends Token {
    private final Version version;
    private final Encoding encoding;

    public DeclarationToken(String data, Version version, Encoding encoding) {
        super(data, Type.DECLARATION);
        this.version = version;
        this.encoding = encoding;
    }

    public Version getVersion() {
        return version;
    }

    public Encoding getEncoding() {
        return encoding;
    }
}
