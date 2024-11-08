package ru.introguzzle.parsers.common.convert;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.xml.entity.XMLAttribute;
import ru.introguzzle.parsers.xml.entity.XMLElement;

import java.util.function.Predicate;

@Getter
@Setter
@AllArgsConstructor
public class XMLElementToJSONConverter implements Converter<XMLElement, JSONObject> {
    private NameConverter nameConverter;
    private String attributePrefix;
    private String textPlaceholder;
    private String characterDataPlaceholder;

    private interface AttributePredicate extends Predicate<XMLAttribute> {
        @Override
        boolean test(XMLAttribute attribute);
    }

    private JSONObject convert(XMLElement element, AttributePredicate predicate) {
        JSONObject object = new JSONObject();

        for (XMLAttribute attribute : element.getAttributes()) {
            if (predicate.test(attribute)) {
                object.put(attributePrefix + nameConverter.apply(attribute.name()), attribute.value());
            }
        }

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

        String data = element.getCharacterData();
        if (data != null && !data.isEmpty()) {
            object.put(characterDataPlaceholder, data);
        }

        return object;
    }

    @Override
    public @NotNull JSONObject convertWithMetadata(@NotNull XMLElement element) {
        return convert(element, _ -> true);
    }

    @Override
    public @NotNull JSONObject convert(@NotNull XMLElement element) {
        return convert(element, attribute -> !attribute.name().startsWith("xmlns"));
    }
}
