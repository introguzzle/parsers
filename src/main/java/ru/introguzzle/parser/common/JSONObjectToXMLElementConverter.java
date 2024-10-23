package ru.introguzzle.parser.common;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parser.json.entity.JSONArray;
import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.xml.XMLAttribute;
import ru.introguzzle.parser.xml.XMLElement;

public class JSONObjectToXMLElementConverter implements JSONObjectToXMLElementMapper {
    private NameConverter nameConverter;
    private String attributePrefix;
    private String defaultRootName;
    private String textPlaceholder;
    private String dataPlaceholder;

    public JSONObjectToXMLElementConverter(NameConverter nameConverter,
                                           String attributePrefix,
                                           String defaultRootName,
                                           String textPlaceholder,
                                           String dataPlaceholder) {
        this.nameConverter = nameConverter;
        this.attributePrefix = attributePrefix;
        this.defaultRootName = defaultRootName;
        this.textPlaceholder = textPlaceholder;
        this.dataPlaceholder = dataPlaceholder;
    }

    @Override
    public @NotNull XMLElement convert(@NotNull JSONObject object, String name) {
        XMLElement element = new XMLElement(name);
        for (String key : object.keySet()) {
            if (key.startsWith(dataPlaceholder)) {
                element.setCData(object.get(key, String.class));
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
                if (value instanceof JSONObject o) {
                    element.addChild(convert(o, nameConverter.apply(key)));
                }

                if (value instanceof JSONArray array) {
                    for (Object item : array) {
                        element.addChild(convert((JSONObject) item, nameConverter.apply(key)));
                    }
                }
            }
        }

        if (element.getText() == null) {
            element.setText("");
        }

        if (element.getCData() == null) {
            element.setCData("");
        }

        return element;
    }

    @Override
    public @NotNull NameConverter getNameConverter() {
        return nameConverter;
    }

    @Override
    public void setNameConverter(@NotNull NameConverter nameConverter) {
        this.nameConverter = nameConverter;
    }

    @Override
    public String getAttributePrefix() {
        return attributePrefix;
    }

    @Override
    public void setAttributePrefix(String attributePrefix) {
        this.attributePrefix = attributePrefix;
    }

    @Override
    public String getDefaultRootName() {
        return defaultRootName;
    }

    @Override
    public void setDefaultRootName(String rootName) {
        this.defaultRootName = rootName;
    }
}
