package ru.introguzzle.parser.common.convert;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.xml.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class is responsible for converting a {@link JSONObject} into an {@link XMLDocument}.
 * It supports conversion of JSON objects to XML format, including handling XML attributes, text content,
 * and different XML encodings and versions.
 */
public class JSONObjectToXMLDocumentConverter implements Converter<JSONObject, XMLDocument> {
    private NameConverter nameConverter;

    @Getter
    @Setter
    private String attributePrefix;

    @Setter
    @Getter
    private String defaultRootName;

    @Getter
    @Setter
    private JSONObjectToXMLElementMapper elementMapper;

    @Getter
    @Setter
    private Version defaultVersion = Version.V1_0;

    @Getter
    @Setter
    private Encoding defaultEncoding = Encoding.UTF_8;

    /**
     * Parameterized constructor to set custom name converter, attribute prefix, and root name.
     *
     * @param nameConverter   The function to convert JSON field names to XML element names.
     * @param attributePrefix The prefix used to identify JSON properties that should be converted to XML attributes.
     * @param defaultRootName The name of the root element in the XML document.
     */
    public JSONObjectToXMLDocumentConverter(NameConverter nameConverter,
                                            String attributePrefix,
                                            String defaultRootName,
                                            JSONObjectToXMLElementMapper elementMapper) {
        this.nameConverter = nameConverter;
        this.attributePrefix = attributePrefix;
        this.defaultRootName = defaultRootName;
        this.elementMapper = elementMapper;
    }

    /**
     * Converts a {@link JSONObject} into an {@link XMLDocument} with potential attribute prefixes.
     *
     * @param object The input JSON object to convert.
     * @return The converted XML document.
     */
    @Override
    public @NotNull XMLDocument convertWithMetadata(@NotNull JSONObject object) {
        return convert(
                object,
                Version.orElse(object.get(attributePrefix + "version", String.class), defaultVersion),
                Encoding.orElse(object.get(attributePrefix + "encoding", String.class), defaultEncoding)
        );
    }

    private @NotNull XMLDocument convert(@NotNull JSONObject object,
                                         @Nullable Version version,
                                         @Nullable Encoding encoding) {
        XMLElement root;

        List<String> nonAttributeKeys = new ArrayList<>();
        for (String key: object.keySet()) {
            if (!key.startsWith(attributePrefix)) {
                nonAttributeKeys.add(key);
            }
        }

        if (nonAttributeKeys.size() == 1) {
            String rootKey = nonAttributeKeys.getFirst();
            Object value = object.get(rootKey);
            String converted = nameConverter.apply(rootKey);
            if (value instanceof JSONObject) {
                root = elementMapper.convert((JSONObject) value, converted);
            } else {
                root = new XMLElement(converted);
                XMLElement child = new XMLElement(rootKey);
                child.setText(value.toString());
                root.addChild(child);
            }
        } else {
            root = elementMapper.convert(object, defaultRootName);
        }

        return new XMLDocument(
                Objects.requireNonNullElse(version, defaultVersion),
                Objects.requireNonNullElse(encoding, defaultEncoding),
                root
        );
    }

    /**
     * Converts a {@link JSONObject} into an exact {@link XMLDocument} with a custom root element name.
     *
     * @param object   The input JSON object.
     * @return The exact XML document.
     */
    @Override
    public @NotNull XMLDocument convert(@NotNull JSONObject object) {
        return convert(object, null, null);
    }

    /**
     * Sets the name converter used for converting JSON keys to XML element names.
     *
     * @param nameConverter The name converter function.
     */
    @Override
    public void setNameConverter(@NotNull NameConverter nameConverter) {
        this.nameConverter = nameConverter;
    }

    /**
     * Gets the name converter used for converting JSON keys to XML element names.
     *
     * @return The current name converter.
     */
    @Override
    public @NotNull NameConverter getNameConverter() {
        return nameConverter;
    }
}
