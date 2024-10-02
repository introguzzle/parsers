package ru.introguzzle.jsonparser;

import ru.introguzzle.jsonparser.entity.JSONArray;
import ru.introguzzle.jsonparser.entity.JSONObject;
import ru.introguzzle.jsonparser.primitive.DefaultPrimitiveTypeConverter;
import ru.introguzzle.jsonparser.primitive.PrimitiveTypeConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that maps raw string into
 * {@code JSONObject},
 * {@code JSONArray},
 * {@code Number}
 * {@code Boolean}
 * {@code String}
 * <br></br>
 * by class
 */
public class DefaultConverter implements Converter {
    private final PrimitiveTypeConverter primitiveTypeConverter = new DefaultPrimitiveTypeConverter();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T map(String data, Class<? extends T> type) {
        if (data.startsWith("{")) {
            return (T) parseObject(data);
        } else if (data.startsWith("[")) {
            return (T) parseArray(data);
        } else {
            return (T) primitiveTypeConverter.map(data);
        }
    }

    private JSONObject parseObject(String data) {
        JSONObject object = new JSONObject();
        data = data.substring(1, data.length() - 1).trim();

        String[] lines = getLines(data);
        for (String line : lines) {
            String[] split = line.split(":", 2);
            String key = split[0].trim().replace("\"", ""); // Убираем кавычки у ключей
            Object value = map(split[1].trim(), JSONObject.class);
            object.put(key, value);
        }

        return object;
    }

    private JSONArray parseArray(String data) {
        JSONArray array = new JSONArray();
        // Убираем квадратные скобки
        data = data.substring(1, data.length() - 1).trim();

        // Разбиваем на элементы
        String[] elements = getLines(data);
        for (String element : elements) {
            array.add(map(element.trim(), JSONArray.class));
        }

        return array;
    }

    // Метод для разбивки строк на элементы объектов или массивов
    private String[] getLines(String data) {
        List<String> entries = new ArrayList<>();
        int bracketCount = 0;
        boolean inQuotes = false;

        StringBuilder entry = new StringBuilder();

        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);

            // Проверка, если мы находимся внутри кавычек
            if (c == '"') {
                inQuotes = !inQuotes;
            }

            // Увеличиваем или уменьшаем счетчик скобок, только если не внутри кавычек
            if (!inQuotes) {
                if (c == '{' || c == '[') {
                    bracketCount++;
                } else if (c == '}' || c == ']') {
                    bracketCount--;
                }

                // Если встретили запятую вне кавычек и вне вложенных структур
                if (c == ',' && bracketCount == 0) {
                    entries.add(entry.toString().trim());
                    entry.setLength(0);
                    continue;
                }
            }

            // Добавляем символ к текущей записи
            entry.append(c);
        }

        if (!entry.isEmpty()) {
            entries.add(entry.toString().trim());
        }

        return entries.toArray(new String[0]);
    }
}
