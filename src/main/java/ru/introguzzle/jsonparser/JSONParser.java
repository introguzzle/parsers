package ru.introguzzle.jsonparser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.jsonparser.convert.Converter;
import ru.introguzzle.jsonparser.convert.DefaultConverter;
import ru.introguzzle.jsonparser.entity.JSONArray;
import ru.introguzzle.jsonparser.entity.JSONObject;
import ru.introguzzle.jsonparser.validation.BracketValidator;
import ru.introguzzle.jsonparser.validation.DefaultBracketValidator;

public class JSONParser implements Parser {

    private final BracketValidator validator = new DefaultBracketValidator();
    private final Converter converter = new DefaultConverter();

    @SuppressWarnings("unchecked")
    @Override
    public <T> T parse(@Nullable String data, @NotNull Class<? extends T> type) {
        if (data == null) return null;
        validator.validate(data);
        if (data.isEmpty()) {
            if (type == JSONObject.class) {
                return (T) new JSONObject();
            }

            if (type == JSONArray.class) {
                return (T) new JSONArray();
            }

            if (type == String.class) {
                return (T) data;
            }

            return null;
        }

        return converter.map(data, type);
    }
}
