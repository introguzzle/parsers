package ru.introguzzle.jsonparser.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.jsonparser.convert.Converter;
import ru.introguzzle.jsonparser.convert.DefaultConverter;
import ru.introguzzle.jsonparser.validation.BracketValidator;
import ru.introguzzle.jsonparser.validation.DefaultBracketValidator;

public class JSONParser implements Parser {

    private final BracketValidator validator = new DefaultBracketValidator();
    private final Converter converter = new DefaultConverter();

    @Override
    public <T> T parse(@Nullable String data, @NotNull Class<? extends T> type) {
        if (data == null) return null;
        validator.validate(data);
        if (data.isEmpty()) {
            return handleEmptyString(data, type);
        }

        return converter.map(data, type);
    }
}
