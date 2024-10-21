package ru.introguzzle.parser.xml.token;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parser.xml.Type;

import java.util.Objects;

public abstract sealed class Token permits AttributeToken, CDataToken, CommentToken, DeclarationToken, ElementTailToken, ElementToken, TextToken {

    private final String data;
    private final Type type;

    public Token(@NotNull String data, @NotNull Type type) {
        this.data = data;
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Token) obj;
        return Objects.equals(this.data, that.data) &&
                Objects.equals(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, type);
    }

    @Override
    public String toString() {
        return "Token[" +
                "data=" + data + ", " +
                "type=" + type + ']';
    }

}
