package ru.introguzzle.parser.common;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parser.common.utilities.NamingUtilities;
import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.xml.XMLAttribute;
import ru.introguzzle.parser.xml.XMLDocument;
import ru.introguzzle.parser.xml.XMLElement;

public class XMLDocumentToJSONConverter implements Converter<XMLDocument, JSONObject> {
    private NameConverter nameConverter;
    private String attributePrefix;
    private Converter<XMLElement, JSONObject> elementConverter;

    public XMLDocumentToJSONConverter(NameConverter nameConverter,
                                      String attributePrefix,
                                      Converter<XMLElement, JSONObject> elementConverter) {
        this.nameConverter = nameConverter;
        this.attributePrefix = attributePrefix;
        this.elementConverter = elementConverter;
    }

    @Override
    public @NotNull JSONObject convertWithMetadata(@NotNull XMLDocument document) {
        JSONObject object = new JSONObject();

        object.put(attributePrefix + "version", document.getVersion().getValue());
        object.put(attributePrefix + "encoding", document.getEncoding().getValue());

        object.put(
                nameConverter.apply(document.getRoot().getName()),
                elementConverter.convertWithMetadata(document.getRoot())
        );

        return object;
    }

    @Override
    public @NotNull JSONObject convert(@NotNull XMLDocument document) {
        JSONObject object = new JSONObject();
        object.put(
                nameConverter.apply(document.getRoot().getName()),
                elementConverter.convert(document.getRoot())
        );

        return object;
    }

    public Converter<XMLElement, JSONObject> getElementConverter() {
        return elementConverter;
    }

    public void setElementConverter(Converter<XMLElement, JSONObject> elementConverter) {
        this.elementConverter = elementConverter;
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
}
