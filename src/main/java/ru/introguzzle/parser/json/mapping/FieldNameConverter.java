package ru.introguzzle.parser.json.mapping;

import lombok.RequiredArgsConstructor;
import ru.introguzzle.parser.common.convert.NameConverter;

@RequiredArgsConstructor
public abstract class FieldNameConverter<T> implements NameConverter {
    private final NameConverter converter;

    @Override
    public String apply(String s) {
        return converter.apply(s);
    }

    public abstract String convert(T field);
}
