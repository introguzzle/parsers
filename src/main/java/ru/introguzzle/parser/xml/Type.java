package ru.introguzzle.parser.xml;

public enum Type {
    ELEMENT_HEAD,
    ELEMENT_TAIL,
    SELF_CLOSING_ELEMENT,
    ATTRIBUTE,     // Атрибут key="value"
    TEXT,          // Текст между элементами
    COMMENT,       // Комментарий <!-- comment -->
    DECLARATION,    // XML-декларация <?xml version="1.0"?>
    CDATA,
}
