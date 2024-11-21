package ru.introguzzle.parsers.common.convert;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.config.Configuration;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.xml.entity.XMLDocument;
import ru.introguzzle.parsers.xml.entity.XMLElement;

import java.lang.reflect.InvocationTargetException;

/**
 * An abstract factory class responsible for creating {@link Converter} instances that facilitate
 * the transformation between XML and JSON representations.
 *
 * <p>The {@code ConverterFactory} class serves as the central point for obtaining various converters
 * necessary for serializing and deserializing XML and JSON data. It leverages the application's
 * configuration settings to determine whether to use a default factory implementation or a custom one
 * specified by the user. This design promotes flexibility and extensibility, allowing developers to
 * inject custom conversion logic as needed.</p>
 *
 * <p><strong>Key Responsibilities:</strong></p>
 * <ul>
 *     <li>Providing converters for transforming {@link XMLDocument} and {@link XMLElement} instances to {@link JSONObject} and vice versa.</li>
 *     <li>Managing the creation and retrieval of name converters for XML and JSON field naming strategies.</li>
 *     <li>Handling configuration-based factory instantiation, supporting both default and custom factory implementations.</li>
 * </ul>
 *
 * <p><strong>Example Usage:</strong></p>
 * <pre>{@code
 * // Obtain the ConverterFactory instance based on configuration
 * ConverterFactory converterFactory = ConverterFactory.getFactory();
 *
 * // Get a converter to transform XMLDocument to JSONObject
 * Converter<XMLDocument, JSONObject> xmlToJsonConverter = converterFactory.getXMLDocumentToJSONConverter();
 *
 * // Convert an XMLDocument to JSONObject
 * XMLDocument xmlDocument = ...; // Assume this is provided
 * JSONObject jsonObject = xmlToJsonConverter.convert(xmlDocument);
 *
 * // Similarly, get a converter to transform JSONObject to XMLDocument
 * Converter<JSONObject, XMLDocument> jsonToXmlConverter = converterFactory.getJSONDocumentToXMLConverter();
 * XMLDocument convertedXmlDocument = jsonToXmlConverter.convert(jsonObject);
 * }</pre>
 *
 * <p><strong>Exception Handling:</strong></p>
 * <ul>
 *     <li><b>{@link ClassCastException}</b>: Thrown if the specified custom factory class does not extend {@code ConverterFactory}.</li>
 *     <li><b>{@link RuntimeException}</b>: Thrown for errors related to class loading, instantiation, or reflection failures when attempting to load a custom factory.</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>The {@code ConverterFactory} class is thread-safe for concurrent read operations. However, the underlying
 * {@code MAPPERS} map in {@link XMLDocument} is not thread-safe. If multiple threads are expected to bind or unbind mappers
 * concurrently, appropriate synchronization mechanisms or thread-safe collections (e.g., {@code ConcurrentHashMap}) should be used.</p>
 *
 * <p><strong>Best Practices:</strong></p>
 * <ul>
 *     <li>Use the {@code getFactory()} method to obtain the appropriate {@code ConverterFactory} instance based on the current configuration.</li>
 *     <li>When implementing custom converters, ensure they adhere to the expected interface contracts to maintain compatibility.</li>
 *     <li>Handle exceptions gracefully, providing meaningful error messages to aid in debugging and maintenance.</li>
 *     <li>Ensure that custom factory classes specified in the configuration are available in the classpath and correctly implement {@code ConverterFactory}.</li>
 * </ul>
 *
 * @see Converter
 * @see ConverterFactoryImpl
 * @see Configuration
 * @see JSONObject
 * @see XMLDocument
 * @see XMLElement
 */
public abstract class ConverterFactory {

    /**
     * The application's configuration instance, loaded at class initialization.
     */
    private static final Configuration CONFIGURATION;

    static {
        CONFIGURATION = Configuration.instance();
    }

    /**
     * Retrieves the appropriate {@code ConverterFactory} instance based on the current configuration.
     *
     * <p>If the configuration indicates that a custom factory class should be used, this method attempts to
     * load and instantiate it via reflection. If the configuration is not loaded or specifies to use the default
     * factory, the default {@link ConverterFactoryImpl} is returned.</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * ConverterFactory factory = ConverterFactory.getFactory();
     * }</pre>
     *
     * @return An instance of {@code ConverterFactory}, either the default implementation or a custom one as specified by the configuration.
     * @throws ClassCastException If the specified custom factory class does not extend {@code ConverterFactory}.
     * @throws RuntimeException If an error occurs during the loading or instantiation of the custom factory class.
     */
    public static @NotNull ConverterFactory getFactory() {
        if (!CONFIGURATION.isLoaded()) {
            return getDefault();
        }

        Configuration.Property<String> factoryClassProperty = CONFIGURATION.getConverterFactoryClassProperty();
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

    /**
     * Creates and returns the default {@code ConverterFactory} implementation.
     *
     * <p>This method initializes a new instance of {@link ConverterFactoryImpl} using configuration
     * properties such as name converters, attribute prefixes, root names, and placeholders for text and character data.</p>
     *
     * @return A new instance of {@code ConverterFactoryImpl} initialized with default configuration settings.
     */
    private static @NotNull ConverterFactory getDefault() {
        return new ConverterFactoryImpl(
                CONFIGURATION.getXMLNameConverterProperty().getValue(),
                CONFIGURATION.getJSONNameConverterProperty().getValue(),
                CONFIGURATION.getAttributePrefixProperty().getValue(),
                CONFIGURATION.getRootNameProperty().getValue(),
                CONFIGURATION.getTextPlaceholderProperty().getValue(),
                CONFIGURATION.getCharacterDataPlaceholderProperty().getValue()
        );
    }

    /**
     * Retrieves a converter that transforms {@link XMLDocument} instances into {@link JSONObject} instances.
     *
     * <p>This converter facilitates the conversion of entire XML documents into their JSON representations,
     * enabling interoperability between XML and JSON data formats.</p>
     *
     * @return A {@code Converter} that converts {@code XMLDocument} to {@code JSONObject}.
     */
    public abstract Converter<XMLDocument, JSONObject> getXMLDocumentToJSONConverter();

    /**
     * Retrieves a converter that transforms {@link XMLElement} instances into {@link JSONObject} instances.
     *
     * <p>This converter facilitates the conversion of individual XML elements into their JSON representations,
     * allowing for granular control over the serialization process.</p>
     *
     * @return A {@code Converter} that converts {@code XMLElement} to {@code JSONObject}.
     */
    public abstract @NotNull Converter<XMLElement, JSONObject> getXMLElementToJSONConverter();

    /**
     * Retrieves a converter that transforms {@link JSONObject} instances into {@link XMLDocument} instances.
     *
     * <p>This converter facilitates the conversion of JSON data into XML document structures, enabling
     * bidirectional data format transformations.</p>
     *
     * @return A {@code Converter} that converts {@code JSONObject} to {@code XMLDocument}.
     */
    public abstract Converter<JSONObject, XMLDocument> getJSONDocumentToXMLConverter();

    /**
     * Retrieves a mapper that transforms {@link JSONObject} instances into {@link XMLElement} instances.
     *
     * <p>This mapper is used to convert JSON objects into corresponding XML elements, supporting the creation
     * of XML structures based on JSON data.</p>
     *
     * @return A {@code JSONObjectToXMLElementMapper} that maps {@code JSONObject} to {@code XMLElement}.
     */
    @NotNull
    public abstract JSONObjectToXMLElementMapper getJSONObjectToXMLElementConverter();

    /**
     * Retrieves the {@link NameConverter} used for converting JSON field names to XML field names.
     *
     * <p>This converter defines the strategy for mapping JSON field naming conventions to XML naming conventions,
     * ensuring consistency and adherence to XML standards.</p>
     *
     * @return The {@code NameConverter} used for XML field name conversions.
     */
    public abstract NameConverter getToXMLConverter();

    /**
     * Retrieves the {@link NameConverter} used for converting XML field names to JSON field names.
     *
     * <p>This converter defines the strategy for mapping XML field naming conventions to JSON naming conventions,
     * facilitating accurate and meaningful JSON representations of XML data.</p>
     *
     * @return The {@code NameConverter} used for JSON field name conversions.
     */
    public abstract NameConverter getToJSONConverter();

    /**
     * Retrieves the prefix used for XML attributes during conversion processes.
     *
     * <p>This prefix is applied to attribute names to distinguish them from element names,
     * ensuring clear differentiation between attributes and elements in the XML structure.</p>
     *
     * @return A {@code String} representing the attribute prefix.
     */
    public abstract String getAttributePrefix();

    /**
     * Retrieves the default root element name used when converting JSON data to XML.
     *
     * <p>If the JSON data does not specify a root element, this default name is used to create the
     * root of the resulting XML document.</p>
     *
     * @return A {@code String} representing the default root element name.
     */
    public abstract String getDefaultRootName();

    /**
     * Retrieves the placeholder string used to represent text content within XML elements.
     *
     * <p>This placeholder is used during the conversion process to handle text nodes,
     * ensuring that textual data within XML elements is appropriately captured and represented.</p>
     *
     * @return A {@code String} representing the text placeholder.
     */
    public abstract String getTextPlaceholder();

    /**
     * Retrieves the placeholder string used to represent character data within XML elements.
     *
     * <p>This placeholder is used during the conversion process to handle character data sections,
     * ensuring that character data within XML elements is appropriately captured and represented.</p>
     *
     * @return A {@code String} representing the character data placeholder.
     */
    public abstract String getCharacterDataPlaceholder();
}
