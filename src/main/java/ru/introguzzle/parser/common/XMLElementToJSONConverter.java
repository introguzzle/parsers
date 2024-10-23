package ru.introguzzle.parser.common;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parser.json.entity.JSONArray;
import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.xml.XMLAttribute;
import ru.introguzzle.parser.xml.XMLElement;

import java.util.function.Predicate;

public class XMLElementToJSONConverter implements Converter<XMLElement, JSONObject> {
    private NameConverter nameConverter;
    private String attributePrefix;
    private String textPlaceholder;
    private String dataPlaceholder;

    public XMLElementToJSONConverter(NameConverter nameConverter,
                                     String attributePrefix,
                                     String textPlaceholder,
                                     String dataPlaceholder) {
        this.nameConverter = nameConverter;
        this.attributePrefix = attributePrefix;
        this.textPlaceholder = textPlaceholder;
        this.dataPlaceholder = dataPlaceholder;
    }

    private interface AttributePredicate extends Predicate<XMLAttribute> {
        @Override
        boolean test(XMLAttribute attribute);
    }

    private JSONObject convert(XMLElement element, AttributePredicate predicate) {
        JSONObject object = new JSONObject();

        for (XMLAttribute attribute : element.getAttributes()) {
            if (predicate.test(attribute)) {
                object.put(attributePrefix + nameConverter.apply(attribute.getName()), attribute.getValue());
            }
        }

        int childCount = element.getChildren().size();
        for (XMLElement child : element.getChildren()) {
            String name = nameConverter.apply(child.getName());

            if (object.containsKey(name)) {
                Object existingElement = object.get(name);
                JSONArray array;

                if (existingElement instanceof JSONArray) {
                    array = (JSONArray) existingElement;
                } else {
                    array = new JSONArray();
                    array.add(existingElement);
                    object.put(name, array);
                }

                array.add(convert(child, predicate));
            } else {
                object.put(name, convert(child, predicate));
            }
        }

        String text = element.getText();
        if (text != null && !text.isEmpty()) {
            object.put(textPlaceholder, text);
        }

        String data = element.getCData();
        if (data != null && !data.isEmpty()) {
            object.put(dataPlaceholder, data);
        }

        return object;
    }

    @Override
    public @NotNull JSONObject convertWithMetadata(@NotNull XMLElement element) {
        return convert(element, _ -> true);
    }

    @Override
    public @NotNull JSONObject convert(@NotNull XMLElement element) {
        return convert(element, attribute -> !attribute.getName().startsWith("xmlns"));
    }

    @NotNull
    @Override
    public NameConverter getNameConverter() {
        return nameConverter;
    }

    @Override
    public void setNameConverter(@NotNull NameConverter nameConverter) {
        this.nameConverter = nameConverter;
    }

    public String getAttributePrefix() {
        return attributePrefix;
    }

    public void setAttributePrefix(String attributePrefix) {
        this.attributePrefix = attributePrefix;
    }

    public String getTextPlaceholder() {
        return textPlaceholder;
    }

    public void setTextPlaceholder(String textPlaceholder) {
        this.textPlaceholder = textPlaceholder;
    }

    public String getDataPlaceholder() {
        return dataPlaceholder;
    }

    public void setDataPlaceholder(String dataPlaceholder) {
        this.dataPlaceholder = dataPlaceholder;
    }
}
