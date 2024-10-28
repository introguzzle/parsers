package ru.introguzzle.parser.xml;

import java.io.Serial;
import java.io.Serializable;

public record XMLAttribute(String name, String value) implements Serializable {
    @Serial
    private static final long serialVersionUID = 3501024352307557330L;

    @Override
    public String toString() {
        return name + "=\"" + value + "\"";
    }
}
