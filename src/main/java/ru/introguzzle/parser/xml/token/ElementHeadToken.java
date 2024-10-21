package ru.introguzzle.parser.xml.token;

import ru.introguzzle.parser.xml.Type;
import ru.introguzzle.parser.xml.XMLElement;

import java.util.ArrayList;
import java.util.List;

public class ElementHeadToken extends ElementToken {
    private final List<Token> children;

    public ElementHeadToken(String name,
                            String data,
                            List<AttributeToken> attributes) {
        this(name, data, attributes, new ArrayList<>());
    }

    public ElementHeadToken(String name,
                            String data,
                            List<AttributeToken> attributes,
                            List<Token> children) {
        super(name, data, Type.ELEMENT_HEAD, attributes);
        this.children = children;
    }


    public final void addChild(Token token) {
        children.add(token);
    }

    public final List<Token> getChildren() {
        return children;
    }

    public final XMLElement toXMLElement() {
        XMLElement element = new XMLElement(getName());
        element.addAttributes(getAttributes()
                .stream()
                .map(AttributeToken::toXMLAttribute)
                .toList()
        );

        element.setText(getChildren()
                .stream()
                .filter(TextToken.class::isInstance)
                .map(Token::getData)
                .reduce(String::concat)
                .orElse("")
        );

        for (Token child : children) {
            if (child instanceof ElementHeadToken headToken) {
                element.addChild(headToken.toXMLElement());
            }
        }

        return element;
    }
}
