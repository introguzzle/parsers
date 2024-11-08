package ru.introguzzle.parsers.common.convert;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.xml.entity.XMLElement;

public interface JSONObjectToXMLElementMapper {
    @NotNull XMLElement convert(@NotNull JSONObject object, String name);

    @NotNull NameConverter getNameConverter();

    void setNameConverter(@NotNull NameConverter nameConverter);

    String getAttributePrefix();

    void setAttributePrefix(String attributePrefix);

    String getDefaultRootName();

    void setDefaultRootName(String rootName);
}
