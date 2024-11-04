package ru.introguzzle.parser.common.field;

import java.lang.reflect.Field;
import java.util.List;

public interface FieldAccessor {
    List<Field> get(Class<?> type);
}
