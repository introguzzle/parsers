package ru.introguzzle.jsonparser.parse.tokenize;

public enum Type {
    OBJECT_START,
    OBJECT_END,
    ARRAY_START,
    ARRAY_END,
    KEY,
    STRING,
    NUMBER,
    BOOLEAN,
    NULL,
    COLON,
    COMMA
}
