package ru.introguzzle.parser.xml;

import java.io.Serial;
import java.io.Serializable;

public enum Type implements Serializable {
    ELEMENT_HEAD,
    ELEMENT_TAIL,
    SELF_CLOSING_ELEMENT,
    ATTRIBUTE,     // Атрибут key="value"
    TEXT,          // Текст между элементами
    COMMENT,       // Комментарий <!-- comment -->
    DECLARATION,    // XML-декларация <?xml version="1.0"?>
    CDATA;

    @Serial
    private static final long serialVersionUID = -5055877539493038583L;
}
