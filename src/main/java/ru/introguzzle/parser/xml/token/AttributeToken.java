package ru.introguzzle.parser.xml.token;

import ru.introguzzle.parser.xml.Type;
import ru.introguzzle.parser.xml.XMLAttribute;

import java.util.Arrays;

public non-sealed class AttributeToken extends Token {
    private final String name;
    private final String value;

    public AttributeToken(String data) {
        super(data, Type.ATTRIBUTE);
        String[] split = getData().split("=", 2);

        if (split.length != 2) {
            throw new AssertionError("Shouldn't happen");
        }

        this.name = split[0];
        this.value = split[1];
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public XMLAttribute toXMLAttribute() {
        return new XMLAttribute(getName(), getValue());
    }
}
