package ru.introguzzle.parsers.json.mapping.reference;

import ru.introguzzle.parsers.json.mapping.MappingException;

public class CircularReferenceException extends MappingException {
    public CircularReferenceException(Object object) {
        super("Circular reference: " + object);
    }
}
