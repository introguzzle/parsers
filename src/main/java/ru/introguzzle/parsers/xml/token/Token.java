package ru.introguzzle.parsers.xml.token;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public abstract sealed class Token implements Serializable permits AttributeToken,
        CharacterDataToken, CommentToken, DeclarationToken,
        ElementTailToken, ElementToken, TextToken {
    @Serial
    private static final long serialVersionUID = -3536243019629556440L;

    private final String data;
    private final Type type;

    @Override
    public String toString() {
        return "Token[" +
                "data=" + data + ", " +
                "type=" + type + ']';
    }
}
