package ru.introguzzle.parser.xml.token;

import java.io.Serial;
import java.io.Serializable;

public enum Type implements Serializable {
    ELEMENT_HEAD,
    ELEMENT_TAIL,
    SELF_CLOSING_ELEMENT,
    ATTRIBUTE,
    TEXT,
    COMMENT,
    DECLARATION,
    CHARACTER_DATA;

    @Serial
    private static final long serialVersionUID = -5055877539493038583L;
}
