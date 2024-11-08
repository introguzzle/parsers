package ru.introguzzle.parsers.json.parse.tokenize;

import java.util.Objects;

public final class Token {
    private final String data;
    private Type type;

    public Token(String data, Type type) {
        this.data = data;
        this.type = type;
    }

    public Token(CharSequence data, Type type) {
        this(data.toString(), type);
    }

    public Token(Character data, Type type) {
        this("" + data, type);
    }

    @Override
    public String toString() {
        return String.format("%s %s", type, data);
    }

    public String getData() {
        return data;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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
}
