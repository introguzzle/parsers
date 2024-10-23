package ru.introguzzle.parser.xml.token;

import ru.introguzzle.parser.xml.Type;
import ru.introguzzle.parser.xml.XMLAttribute;
import ru.introguzzle.parser.xml.XMLParseException;

import java.io.Serial;

public non-sealed class AttributeToken extends Token {
    @Serial
    private static final long serialVersionUID = 1521597783326870820L;

    private final String name;
    private final String value;

    public AttributeToken(String data) {
        super(data, Type.ATTRIBUTE);
        String[] split = getData().split("=", 2);

        if (split.length != 2) {
            throw new XMLParseException("Invalid attribute syntax");
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
