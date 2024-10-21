package ru.introguzzle.parser.xml;

public enum Version {
    V1_0("1.0"),
    V1_1("1.1");

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
            default -> throw new IllegalStateException("Unknown version: " + version);
        };
    }
}
