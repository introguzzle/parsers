package ru.introguzzle.parser.json.mapping.reference;

import ru.introguzzle.parser.json.mapping.MappingException;

public class CircularReferenceException extends MappingException {
    public CircularReferenceException(Object object) {
        super("Circular reference: " + object);
    }
}
