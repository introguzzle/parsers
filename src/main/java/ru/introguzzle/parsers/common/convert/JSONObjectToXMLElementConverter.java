package ru.introguzzle.parsers.common.convert;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.json.entity.JSONArray;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.xml.entity.XMLAttribute;
import ru.introguzzle.parsers.xml.entity.XMLElement;

@Getter
@Setter
@AllArgsConstructor
public class JSONObjectToXMLElementConverter implements JSONObjectToXMLElementMapper {
    private NameConverter nameConverter;
    private String attributePrefix;
    private String defaultRootName;
    private String textPlaceholder;
    private String characterDataPlaceholder;

    @Override
    public @NotNull XMLElement convert(@NotNull JSONObject object, String name) {
        XMLElement element = new XMLElement(name);
        for (String key : object.keySet()) {
            if (key.startsWith(characterDataPlaceholder)) {
                element.setCharacterData(object.get(key, String.class));
                continue;
            }

            if (key.startsWith(attributePrefix)) {
                if (key.equals(textPlaceholder)) {
                    element.setText(object.get(key, String.class));
                    continue;
                }

                element.addAttribute(new XMLAttribute(
                        key.substring(attributePrefix.length()),
                        object.get(key, String.class)
                ));

            } else {
                Object value = object.get(key);
                String converted = nameConverter.apply(key);

                if (value instanceof JSONObject o) {
                    element.addChild(convert(o, converted));
                } else if (value instanceof JSONArray array) {
                    for (Object item : array) {
                        if (item instanceof JSONObject) {
                            element.addChild(convert((JSONObject) item, converted));
                        } else if (item instanceof String) {
                            XMLElement child = new XMLElement(converted);
                            child.setText((String) item);
                            element.addChild(child);
                        }
                    }
                } else {
                    XMLElement child = new XMLElement(converted);
                    if (value != null) {
                        child.setText(value.toString());
                    }

                    element.addChild(child);
                }
            }
        }

        if (element.getText() == null) {
            element.setText("");
        }

        if (element.getCharacterData() == null) {
            element.setCharacterData("");
        }

        return element;
    }
}
