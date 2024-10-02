package ru.introguzzle.jsonparser.mapping.context;

public class MappingContext {
    public CircularReferenceStrategy circularReferenceStrategy;

    public MappingContext(CircularReferenceStrategy circularReferenceStrategy) {
        this.circularReferenceStrategy = circularReferenceStrategy;
    }

    public static final MappingContext DEFAULT = new MappingContext(
            CircularReferenceStrategy.THROW_EXCEPTION
    );
}
