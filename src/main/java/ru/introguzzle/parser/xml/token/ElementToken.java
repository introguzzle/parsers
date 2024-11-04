package ru.introguzzle.parser.xml.token;

import lombok.Getter;

import java.io.Serial;
import java.util.List;

@Getter
public abstract non-sealed class ElementToken extends Token {
    @Serial
    private static final long serialVersionUID = 1090634404338707380L;

    private final String name;
    private final List<AttributeToken> attributes;

    public ElementToken(String name,
                        String data,
                        Type type,
                        List<AttributeToken> attributes) {
        super(data, type);
        this.name = name;
        this.attributes = attributes;
    }
}
