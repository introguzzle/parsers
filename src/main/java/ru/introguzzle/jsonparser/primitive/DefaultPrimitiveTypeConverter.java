package ru.introguzzle.jsonparser.primitive;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.jsonparser.utilities.NumberUtilities;

public class DefaultPrimitiveTypeConverter implements PrimitiveTypeConverter {

    @Override
    public Object apply(@NotNull String data) {
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

        if (NumberUtilities.isNumeric(data)) {
            return Double.parseDouble(data);
        } else {
            return data.replace("\"", "");
        }
    }
}
