package ru.introguzzle.jsonparser;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtilities {
    public static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        while (type != null && type != Object.class) {
            Field[] declaredFields = type.getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                fields.addLast(field);
            }
            type = type.getSuperclass();
        }

        return fields;
    }
}
