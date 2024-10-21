package ru.introguzzle.parser.json.parse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parser.json.convert.Converter;
import ru.introguzzle.parser.json.convert.ConverterImpl;
import ru.introguzzle.parser.json.validation.BracketValidator;
import ru.introguzzle.parser.json.validation.BracketValidatorImpl;

public class JSONParser extends Parser {
    private final BracketValidator validator;
    private final Converter converter;

    public JSONParser() {
        this(new BracketValidatorImpl(), new ConverterImpl());
    }

    public JSONParser(BracketValidator validator, Converter converter) {
        this.validator = validator;
        this.converter = converter;
    }

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
