package ru.introguzzle.parser.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.xml.*;

import java.util.Objects;

/**
 * This class is responsible for converting a {@link JSONObject} into an {@link XMLDocument}.
 * It supports conversion of JSON objects to XML format, including handling XML attributes, text content,
 * and different XML encodings and versions.
 */
public class JSONObjectToXMLDocumentConverter implements Converter<JSONObject, XMLDocument> {

    private NameConverter nameConverter;
    private String attributePrefix;
    private String defaultRootName;
    private JSONObjectToXMLElementMapper elementMapper;

    private Version defaultVersion = Version.V1_0;
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
        XMLElement root = null;

        for (String key : object.keySet()) {
            if (!key.startsWith(attributePrefix)) {
                Object value = object.get(key);
                if (value instanceof JSONObject) {
                    root = elementMapper.convert((JSONObject) value, nameConverter.apply(key));
                }
            }
        }

        if (root == null) {
            throw new RuntimeException("Root element not found");
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

    /**
     * Sets the prefix used for identifying attributes in the JSON object.
     *
     * @param attributePrefix The prefix for attributes (e.g., "@").
     */
    public void setAttributePrefix(String attributePrefix) {
        this.attributePrefix = attributePrefix;
    }

    /**
     * Gets the prefix used for identifying attributes in the JSON object.
     *
     * @return The attribute prefix.
     */
    public String getAttributePrefix() {
        return attributePrefix;
    }

    /**
     * Sets the mapper used for mapping JSON objects to XML elements.
     *
     * @param elementMapper The element converter.
     */
    public void setElementMapper(JSONObjectToXMLElementMapper elementMapper) {
        this.elementMapper = elementMapper;
    }

    /**
     * Gets the mapper used for mapping JSON objects to XML elements.
     *
     * @return The element converter.
     */
    public JSONObjectToXMLElementMapper getElementMapper() {
        return elementMapper;
    }

    /**
     * Sets the default version of the XML document (e.g., 1.0).
     *
     * @param defaultVersion The version to set.
     */
    public void setDefaultVersion(Version defaultVersion) {
        this.defaultVersion = defaultVersion;
    }

    /**
     * Gets the default version of the XML document.
     *
     * @return The XML version.
     */
    public Version getDefaultVersion() {
        return defaultVersion;
    }

    /**
     * Sets the default encoding of the XML document (e.g., UTF-8).
     *
     * @param defaultEncoding The encoding to set.
     */
    public void setDefaultEncoding(Encoding defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    /**
     * Gets the default encoding of the XML document.
     *
     * @return The XML encoding.
     */
    public Encoding getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Gets the default root element name of the XML document.
     *
     * @return The root element name.
     */
    public String getDefaultRootName() {
        return defaultRootName;
    }

    /**
     * Sets the default root element name of the XML document.
     *
     * @param defaultRootName The root element name to set.
     */
    public void setDefaultRootName(String defaultRootName) {
        this.defaultRootName = defaultRootName;
    }
}
