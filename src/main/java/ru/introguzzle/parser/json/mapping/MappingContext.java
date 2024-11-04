package ru.introguzzle.parser.json.mapping;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.introguzzle.parser.json.mapping.reference.CircularReferenceStrategy;

import java.util.IdentityHashMap;
import java.util.Map;

import static ru.introguzzle.parser.json.mapping.reference.StandardCircularReferenceStrategies.THROW_EXCEPTION;

@Getter
@RequiredArgsConstructor
public class MappingContext {
    private final CircularReferenceStrategy circularReferenceStrategy;

    private final Map<Object, Boolean> references = new IdentityHashMap<>();

    public static MappingContext getDefault() {
        return new MappingContext(THROW_EXCEPTION);
    }

    @SuppressWarnings("ALL")
    public boolean putReference(Object reference) {
        return getReferences().put(reference, Boolean.TRUE) != null;
    }

    public boolean containsReference(Object reference) {
        return getReferences().containsKey(reference);
    }
}
