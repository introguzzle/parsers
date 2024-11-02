package ru.introguzzle.parser.json.mapping;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.introguzzle.parser.json.mapping.reference.CircularReferenceStrategy;
import ru.introguzzle.parser.json.mapping.reference.StandardCircularReferenceStrategies;

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

    public boolean putReference(Object reference) {
        return getReferences().put(reference, Boolean.TRUE) != null;
    }

    public boolean containsReference(Object reference) {
        return getReferences().containsKey(reference);
    }
}
