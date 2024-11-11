package ru.introguzzle.parsers.xml.mapping.serialization;

public class DefaultInflector implements Inflector {
    @Override
    public String singularize(String string) {
        if (string.endsWith("s")) {
            return string.substring(0, string.length() - 1);
        }

        return string.contains("_")
                ? string.concat("_item")
                : string.concat("Item");
    }
}
