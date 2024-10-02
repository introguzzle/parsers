package ru.introguzzle.jsonparser.convert.primitive;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.jsonparser.convert.ConversionException;
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

        if (type == Number.class) {
            if (NumberUtilities.isNumeric(data)) return Double.parseDouble(data);
            throw new ConversionException("Not a numeric value: " + data);

        } else {
            return data.replace("\"", "");
        }
    }
}
