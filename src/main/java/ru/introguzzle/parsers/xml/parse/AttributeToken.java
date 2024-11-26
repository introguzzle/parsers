package ru.introguzzle.parsers.xml.parse;

import lombok.Getter;
import ru.introguzzle.parsers.xml.entity.XMLAttribute;

import java.io.Serial;

@Getter
non-sealed class AttributeToken extends Token {
    @Serial
    private static final long serialVersionUID = 1521597783326870820L;

    private final String name;
    private final String value;

    public AttributeToken(String data) {
        super(data, Type.ATTRIBUTE);
        String[] split = getData().split("=", 2);

        if (split.length != 2) {
            throw new XMLParseException("Invalid attribute syntax: " + data);
        }

        this.name = split[0];
        this.value = split[1];
    }

    public XMLAttribute toXMLAttribute() {
        return new XMLAttribute(getName(), getValue());
    }
}
