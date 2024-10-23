package ru.introguzzle.parser.common;

import ru.introguzzle.parser.common.utilities.NamingUtilities;
import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.xml.XMLDocument;

public class ConverterFactory {
    private final NameConverter toXMLConverter;
    private final NameConverter toJSONConverter;
    private final String attributePrefix;
    private final String defaultRootName;
    private final String textPlaceholder;
    private final String cDataPlaceholder;

    public ConverterFactory(NameConverter toXMLConverter,
                            NameConverter toJSONConverter,
                            String attributePrefix,
                            String defaultRootName,
                            String textPlaceholder,
                            String cDataPlaceholder) {
        this.toXMLConverter = toXMLConverter;
        this.toJSONConverter = toJSONConverter;
        this.attributePrefix = attributePrefix;
        this.defaultRootName = defaultRootName;
        this.textPlaceholder = textPlaceholder;
        this.cDataPlaceholder = cDataPlaceholder;
    }

    public static ConverterFactory getDefaultFactory() {
        return new ConverterFactory(
                NamingUtilities::toCamelCase,
                NamingUtilities::toSnakeCase,
                "@",
                "root",
                "@text",
                "@cdata"
        );
    }

    public Converter<XMLDocument, JSONObject> getXMLDocumentToJSONConverter() {
        return new XMLDocumentToJSONConverter(
                toJSONConverter, attributePrefix,
                new XMLElementToJSONConverter(
                        toJSONConverter, attributePrefix, textPlaceholder, cDataPlaceholder
                )
        );
    }

    public Converter<JSONObject, XMLDocument> getJSONDocumentToXMLConverter() {
        return new JSONObjectToXMLDocumentConverter(
                toXMLConverter, attributePrefix, defaultRootName,
                new JSONObjectToXMLElementConverter(
                        toXMLConverter, attributePrefix, defaultRootName,
                        textPlaceholder, cDataPlaceholder
                )
        );
    }
}
