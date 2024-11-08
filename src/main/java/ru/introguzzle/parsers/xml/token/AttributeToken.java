package ru.introguzzle.parsers.xml.token;

import lombok.Getter;
import ru.introguzzle.parsers.xml.entity.XMLAttribute;
import ru.introguzzle.parsers.xml.parse.XMLParseException;

import java.io.Serial;

@Getter
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

    public XMLAttribute toXMLAttribute() {
        return new XMLAttribute(getName(), getValue());
    }
}
