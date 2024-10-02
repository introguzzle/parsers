package ru.introguzzle.jsonparser.entity;

import java.io.Serial;
import java.util.*;

public class JSONArray extends ArrayList<Object> implements JSONStringConvertable {
    @Serial
    private static final long serialVersionUID = -1731069894963023770L;

    public <T> T get(int index, Class<? extends T> type) {
        return type.cast(get(index));
    }

    public <T> T get(int index, Class<? extends T> type, T defaultValue) {
        Object value = get(index);
        if (value == null) {
            return defaultValue;
        }

        return type.cast(value);
    }

    @Override
    public Iterator<?> getIterator() {
        return iterator();
    }

    @Override
    public String getOpeningSymbol() {
        return "[";
    }

    @Override
    public String getClosingSymbol() {
        return "]";
    }
}
