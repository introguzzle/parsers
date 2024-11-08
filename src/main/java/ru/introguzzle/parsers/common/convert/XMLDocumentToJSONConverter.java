package ru.introguzzle.parsers.common.convert;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.xml.entity.XMLDocument;
import ru.introguzzle.parsers.xml.entity.XMLElement;

@Getter
@Setter
@AllArgsConstructor
public class XMLDocumentToJSONConverter implements Converter<XMLDocument, JSONObject> {
    private NameConverter nameConverter;
    private String attributePrefix;
    private Converter<XMLElement, JSONObject> elementConverter;

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
}
