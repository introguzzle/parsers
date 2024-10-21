package ru.introguzzle.parser.xml;

public enum Encoding {
    UTF_8("UTF-8"),
    ISO_8859_1("ISO-8859-1");

    private final String value;

    Encoding(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Encoding of(String version) {
        return switch (version) {
            case "UTF-8"      -> Encoding.UTF_8;
            case "ISO-8859-1" -> Encoding.ISO_8859_1;
            default -> throw new IllegalStateException("Unknown value: " + version);
        };
    }
}
