package ru.introguzzle.parsers.xml.parse;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.xml.entity.XMLElement;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Getter
class ElementHeadToken extends ElementToken {
    @Serial
    private static final long serialVersionUID = -4985289472805806457L;

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

    public final XMLElement toXMLElement() {
        XMLElement element = new XMLElement(getName());
        element.addAttributes(getAttributes()
                .stream()
                .map(AttributeToken::toXMLAttribute)
                .toList()
        );

        element.setText(reduce(TextToken.class, TextToken::getData));
        element.setCharacterData(reduce(CharacterDataToken.class, CharacterDataToken::getText));
        for (Token child : children) {
            if (child instanceof ElementHeadToken headToken) {
                element.addChild(headToken.toXMLElement());
            }
        }

        return element;
    }

    private <T extends Token> @NotNull String reduce(@NotNull Class<? extends T> type,
                                                     @NotNull Function<? super T, String> mapper) {
        return getChildren()
                .stream()
                .filter(type::isInstance)
                .map(type::cast)
                .map(mapper)
                .reduce(String::concat)
                .orElse("");
    }
}
