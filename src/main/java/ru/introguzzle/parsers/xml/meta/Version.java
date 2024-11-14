package ru.introguzzle.parsers.xml.meta;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.introguzzle.parsers.xml.parse.XMLParseException;

import java.io.Serial;
import java.io.Serializable;

@Getter
@RequiredArgsConstructor
public enum Version implements Serializable {
    V1_0("1.0"),
    V1_1("1.1");

    @Serial
    private static final long serialVersionUID = 6310205669527438560L;

    private final String value;

    public static Version of(String version) {
        return switch (version) {
            case "1.0" -> Version.V1_0;
            case "1.1" -> Version.V1_1;
            default -> throw new XMLParseException("Unknown version: " + version);
        };
    }

    public static Version orElse(String version, Version fallback) {
        try {
            return of(version);
        } catch (NullPointerException | XMLParseException e) {
            return fallback;
        }
    }
}
