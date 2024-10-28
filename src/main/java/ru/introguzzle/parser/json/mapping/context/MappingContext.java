package ru.introguzzle.parser.json.mapping.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.IdentityHashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class MappingContext {
    private final Map<Object, Boolean> references = new IdentityHashMap<>();
    private final CircularReferenceStrategy circularReferenceStrategy;

    public static MappingContext getDefault() {
        return new MappingContext(StandardCircularReferenceStrategies.THROW_EXCEPTION);
    }
}
