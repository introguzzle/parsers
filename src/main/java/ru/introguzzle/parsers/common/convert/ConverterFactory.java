package ru.introguzzle.parsers.common.convert;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.config.Configuration;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.xml.entity.XMLDocument;
import ru.introguzzle.parsers.xml.entity.XMLElement;

import java.lang.reflect.InvocationTargetException;

public abstract class ConverterFactory {
    private static final Configuration CONFIGURATION;
    static {
        CONFIGURATION = Configuration.instance();
    }

    public static @NotNull ConverterFactory getFactory() {
        if (!CONFIGURATION.isLoaded()) {
            return getDefault();
        }

        Configuration.Property<String> factoryClassProperty = CONFIGURATION.getConverterFactoryClass();
        if (factoryClassProperty.isUsingDefaultValue()) {
            return getDefault();
        }

        String factoryClassValue = factoryClassProperty.getValue();

        try {
            Class<?> factoryClass = Class.forName(factoryClassValue);

            try {
                return (ConverterFactory) factoryClass.getConstructor().newInstance();
            } catch (ClassCastException e) {
                throw new ClassCastException(factoryClassValue + " is not an instance of ConverterFactory");
            }
        } catch (ClassNotFoundException | InstantiationException |
                 InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Error loading ConverterFactory class: " + factoryClassValue, e);
        }
    }

    private static @NotNull ConverterFactory getDefault() {
        return new ConverterFactoryImpl(
                CONFIGURATION.getXMLNameConverter().getValue(),
                CONFIGURATION.getJSONNameConverter().getValue(),
                CONFIGURATION.getAttributePrefix().getValue(),
                CONFIGURATION.getRootName().getValue(),
                CONFIGURATION.getTextPlaceholder().getValue(),
                CONFIGURATION.getCharacterDataPlaceholder().getValue()
        );
    }

    public abstract Converter<XMLDocument, JSONObject> getXMLDocumentToJSONConverter();

    public abstract @NotNull Converter<XMLElement, JSONObject> getXMLElementToJSONConverter();

    public abstract Converter<JSONObject, XMLDocument> getJSONDocumentToXMLConverter();

    @NotNull
    public abstract JSONObjectToXMLElementMapper getJSONObjectToXMLElementConverter();

    public abstract NameConverter getToXMLConverter();

    public abstract NameConverter getToJSONConverter();

    public abstract String getAttributePrefix();

    public abstract String getDefaultRootName();

    public abstract String getTextPlaceholder();

    public abstract String getCharacterDataPlaceholder();
}
