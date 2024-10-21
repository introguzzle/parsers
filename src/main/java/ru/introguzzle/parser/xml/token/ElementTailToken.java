package ru.introguzzle.parser.xml.token;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parser.xml.Type;

public non-sealed class ElementTailToken extends Token {
    private final String name;

    public ElementTailToken(@NotNull String name, @NotNull String data) {
        super(data, Type.ELEMENT_TAIL);
        this.name = name;
    }

    public final String getName() {
        return name;
    }
}
