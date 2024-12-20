package ru.introguzzle.parsers.xml.parse;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

@Getter
non-sealed class ElementTailToken extends Token {
    @Serial
    private static final long serialVersionUID = -8333004118487882595L;

    private final String name;

    public ElementTailToken(@NotNull String name, @NotNull String data) {
        super(data, Type.ELEMENT_TAIL);
        this.name = name;
    }
}
