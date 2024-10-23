package ru.introguzzle.parser.common;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.xml.XMLElement;

public interface JSONObjectToXMLElementMapper {
    @NotNull XMLElement convert(@NotNull JSONObject object, String name);

    @NotNull NameConverter getNameConverter();

    void setNameConverter(@NotNull NameConverter nameConverter);

    String getAttributePrefix();

    void setAttributePrefix(String attributePrefix);

    String getDefaultRootName();

    void setDefaultRootName(String rootName);
}
