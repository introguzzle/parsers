package ru.introguzzle.parser.json.convert.primitive;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parser.common.utilities.NumberUtilities;
import ru.introguzzle.parser.json.parse.JSONParseException;

public class PrimitiveTypeConverterImpl implements PrimitiveTypeConverter {

    @Override
    public Object apply(@NotNull String data, @NotNull Class<?> type) {
        switch (data) {
            case "false" -> {
                return Boolean.FALSE;
            }

            case "true" -> {
                return Boolean.TRUE;
            }

            case "null" -> {
                return null;
            }
        }

        String unescaped = unescape(data);

        if (type == Number.class) {
            return Double.parseDouble(unescaped);
        }

        if (data.startsWith("\"") && data.endsWith("\"")) {
            return unescaped;
        }

        if (NumberUtilities.isNumeric(unescaped)) {
            return Double.parseDouble(unescaped);
        }

        throw new JSONParseException("Not a numeric or string value: " + data);
    }

    private static String unescape(String data) {
        return data.replace("\"", "");
    }
}
