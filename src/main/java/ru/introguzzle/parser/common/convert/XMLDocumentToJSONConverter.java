package ru.introguzzle.parser.common.convert;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.xml.XMLDocument;
import ru.introguzzle.parser.xml.XMLElement;

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
