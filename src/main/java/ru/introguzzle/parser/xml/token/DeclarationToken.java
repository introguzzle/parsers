package ru.introguzzle.parser.xml.token;

import lombok.Getter;
import ru.introguzzle.parser.xml.meta.Encoding;
import ru.introguzzle.parser.xml.meta.Version;

import java.io.Serial;

@Getter
public non-sealed class DeclarationToken extends Token {
    @Serial
    private static final long serialVersionUID = -2475454735183998374L;

    private final Version version;
    private final Encoding encoding;

    public DeclarationToken(String data, Version version, Encoding encoding) {
        super(data, Type.DECLARATION);
        this.version = version;
        this.encoding = encoding;
    }
}
