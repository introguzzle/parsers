package ru.introguzzle.parser.json.mapping.context;

public class MappingContext {
    public final CircularReferenceStrategy circularReferenceStrategy;

    public MappingContext(CircularReferenceStrategy circularReferenceStrategy) {
        this.circularReferenceStrategy = circularReferenceStrategy;
    }

    public static final MappingContext DEFAULT = new MappingContext(
            CircularReferenceStrategy.THROW_EXCEPTION
    );
}
