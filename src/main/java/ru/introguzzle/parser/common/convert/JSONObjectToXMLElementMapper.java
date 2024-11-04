package ru.introguzzle.parser.common.convert;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.xml.entity.XMLElement;

public interface JSONObjectToXMLElementMapper {
    @NotNull XMLElement convert(@NotNull JSONObject object, String name);

    @NotNull NameConverter getNameConverter();

    void setNameConverter(@NotNull NameConverter nameConverter);

    String getAttributePrefix();

    void setAttributePrefix(String attributePrefix);

    String getDefaultRootName();

    void setDefaultRootName(String rootName);
}
