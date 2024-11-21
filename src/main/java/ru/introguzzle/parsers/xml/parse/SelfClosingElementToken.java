package ru.introguzzle.parsers.xml.parse;

import java.io.Serial;
import java.util.List;

class SelfClosingElementToken extends ElementToken {
    @Serial
    private static final long serialVersionUID = 2145722705005714923L;

    public SelfClosingElementToken(String name,
                                   String data,
                                   List<AttributeToken> attributes) {
        super(name, data, Type.SELF_CLOSING_ELEMENT, attributes);
    }
}
