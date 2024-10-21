package ru.introguzzle.parser.xml.token;

import ru.introguzzle.parser.xml.Type;

import java.util.List;

public class SelfClosingElementToken extends ElementToken {
    public SelfClosingElementToken(String name,
                                   String data,
                                   List<AttributeToken> attributes) {
        super(name, data, Type.SELF_CLOSING_ELEMENT, attributes);
    }
}
