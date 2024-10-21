package ru.introguzzle.parser.xml;

public final class XMLAttribute {
    private final String name;
    private final String value;

    public XMLAttribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return name + "=\"" + value + "\"";
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
