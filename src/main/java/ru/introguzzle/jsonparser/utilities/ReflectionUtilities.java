package ru.introguzzle.jsonparser.utilities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class ReflectionUtilities {
    private ReflectionUtilities() {}

    /**
     * Retrieves all fields of the specified class, including fields from its superclasses,
     * regardless of the access modifier (private, protected, public).
     *
     * @param type the class from which to retrieve all fields
     * @return a list of all fields in the hierarchy of the class
     */
    public static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        while (type != null && type != Object.class) {
            Field[] declaredFields = type.getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true); // Allows access to private fields
                fields.add(field); // Corrected from addLast to add
            }
            type = type.getSuperclass(); // Move to the superclass
        }

        return fields;
    }
}
