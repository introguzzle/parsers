package ru.introguzzle.parser.xml;

import java.io.Serial;
import java.io.Serializable;

public enum Version implements Serializable {
    V1_0("1.0"),
    V1_1("1.1");

    @Serial
    private static final long serialVersionUID = 6310205669527438560L;

    private final String value;

    Version(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Version of(String version) {
        return switch (version) {
            case "1.0" -> Version.V1_0;
            case "1.1" -> Version.V1_1;
            default -> throw new XMLParseException("Unknown version: " + version);
        };
    }

    public static Version orElse(String version, Version defaultVersion) {
        try {
            return of(version);
        } catch (XMLParseException e) {
            return defaultVersion;
        }
    }
}
