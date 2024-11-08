package ru.introguzzle.parsers.common.convert;

import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.xml.entity.XMLDocument;
import ru.introguzzle.parsers.xml.entity.XMLElement;

@Getter(value = AccessLevel.PUBLIC)
class ConverterFactoryImpl extends ConverterFactory {
    final NameConverter toXMLConverter;
    final NameConverter toJSONConverter;
    final String attributePrefix;
    final String defaultRootName;
    final String textPlaceholder;
    final String characterDataPlaceholder;

    ConverterFactoryImpl(@NotNull NameConverter toXMLConverter,
                         @NotNull NameConverter toJSONConverter,
                         @NotNull String attributePrefix,
                         @NotNull String defaultRootName,
                         @NotNull String textPlaceholder,
                         @NotNull String characterDataPlaceholder) {
        this.toXMLConverter = toXMLConverter;
        this.toJSONConverter = toJSONConverter;
        this.attributePrefix = attributePrefix;
        this.defaultRootName = defaultRootName;
        this.textPlaceholder = textPlaceholder;
        this.characterDataPlaceholder = characterDataPlaceholder;
    }

    @Override
    public Converter<XMLDocument, JSONObject> getXMLDocumentToJSONConverter() {
        return new XMLDocumentToJSONConverter(
                toJSONConverter, attributePrefix,
                getXMLElementToJSONConverter()
        );
    }

    @Override
    public @NotNull Converter<XMLElement, JSONObject> getXMLElementToJSONConverter() {
        return new XMLElementToJSONConverter(
                toJSONConverter, attributePrefix, textPlaceholder, characterDataPlaceholder
        );
    }

    @Override
    public Converter<JSONObject, XMLDocument> getJSONDocumentToXMLConverter() {
        return new JSONObjectToXMLDocumentConverter(
                toXMLConverter, attributePrefix, defaultRootName,
                getJSONObjectToXMLElementConverter()
        );
    }

    @Override
    @NotNull
    public JSONObjectToXMLElementMapper getJSONObjectToXMLElementConverter() {
        return new JSONObjectToXMLElementConverter(
                toXMLConverter, attributePrefix, defaultRootName,
                textPlaceholder, characterDataPlaceholder
        );
    }
}
