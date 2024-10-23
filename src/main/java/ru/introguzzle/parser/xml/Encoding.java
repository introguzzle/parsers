package ru.introguzzle.parser.xml;

import java.io.Serial;
import java.io.Serializable;

public enum Encoding implements Serializable {
    UTF_8("UTF-8"),
    ISO_8859_1("ISO-8859-1");

    @Serial
    private static final long serialVersionUID = -600626579180885352L;

    private final String value;

    Encoding(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Encoding of(String encoding) {
        return switch (encoding) {
            case "UTF-8"      -> Encoding.UTF_8;
            case "ISO-8859-1" -> Encoding.ISO_8859_1;
            default -> throw new XMLParseException("Unknown value: " + encoding);
        };
    }

    public static Encoding orElse(String encoding, Encoding defaultEncoding) {
        try {
            return of(encoding);
        } catch (XMLParseException e) {
            return defaultEncoding;
        }
    }
}
