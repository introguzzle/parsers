package ru.introguzzle.jsonparser.convert.primitive;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.jsonparser.parse.JSONParseException;
import ru.introguzzle.jsonparser.utilities.NumberUtilities;

public class DefaultPrimitiveTypeConverter implements PrimitiveTypeConverter {

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
            return unescaped;  // Return unescaped string
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
