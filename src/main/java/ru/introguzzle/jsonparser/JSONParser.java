package ru.introguzzle.jsonparser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.jsonparser.validation.BracketValidator;
import ru.introguzzle.jsonparser.validation.DefaultBracketValidator;

public class JSONParser implements Parser {

    private final BracketValidator validator = new DefaultBracketValidator();
    private final Converter converter = new DefaultConverter();

    @Override
    public <T> T parse(@Nullable String data, @NotNull Class<? extends T> type) {
        if (data == null || data.isEmpty()) return null;
        validator.validate(data);
        return converter.map(data, type);
    }
}
