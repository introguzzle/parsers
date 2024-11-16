package ru.introguzzle.parsers.xml.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.convert.ConverterFactory;
import ru.introguzzle.parsers.common.convert.Converter;
import ru.introguzzle.parsers.common.mapping.MappingException;
import ru.introguzzle.parsers.common.visit.Visitable;
import ru.introguzzle.parsers.common.visit.Visitor;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.json.mapping.JSONObjectConvertable;
import ru.introguzzle.parsers.xml.mapping.deserialization.ObjectMapper;
import ru.introguzzle.parsers.xml.meta.Encoding;
import ru.introguzzle.parsers.xml.meta.Version;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an XML document and provides functionalities for converting between XML and JSON representations,
 * as well as deserializing XML data into Java objects.
 *
 * <p>The {@code XMLDocument} class serves as a foundational component within the deserialization framework,
 * enabling the transformation of XML data into various formats and Java object structures. It implements
 * multiple interfaces to support conversion to JSON objects, XML strings, and visitation patterns.</p>
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *     <li>Serialization and deserialization between XML and JSON formats.</li>
 *     <li>Conversion of XML data into Java objects using registered {@link ObjectMapper} instances.</li>
 *     <li>Support for versioning and encoding metadata.</li>
 *     <li>Integration with the Visitor design pattern for traversing XML documents.</li>
 *     <li>Caching and management of {@link ObjectMapper} instances for efficient deserialization.</li>
 * </ul>
 *
 * <p><strong>Example Usage:</strong></p>
 * <pre>{@code
 * // Create an XMLDocument instance with version, encoding, and root element
 * Version version = Version.V1_0;
 * Encoding encoding = Encoding.UTF_8;
 * XMLElement rootElement = new XMLElement("company");
 * XMLDocument xmlDocument = new XMLDocument(version, encoding, rootElement);
 *
 * // Convert XMLDocument to JSON
 * JSONObject jsonObject = xmlDocument.toJSONObject();
 *
 * // Register an ObjectMapper for the Company class
 * ObjectMapper companyMapper = new CompanyObjectMapper();
 * XMLDocument.bindTo(Company.class, companyMapper);
 *
 * // Deserialize XMLDocument to a Company object
 * Company company = xmlDocument.toObject(Company.class);
 * }</pre>
 *
 * <p><strong>Exception Handling:</strong></p>
 * <ul>
 *     <li><b>{@link MappingException}</b>: Thrown when deserialization fails due to issues like missing mappers,
 *     type mismatches, or other mapping-related errors.</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>The {@code XMLDocument} class maintains a static map of {@link ObjectMapper} instances, which is not thread-safe.
 * If multiple threads are expected to register or deregister mappers concurrently, consider synchronizing access
 * to the {@code MAPPERS} map or using a thread-safe map implementation.</p>
 *
 * @see JSONObject
 * @see ObjectMapper
 * @see MappingException
 * @see ConverterFactory
 * @see Converter
 * @see Visitable
 * @see Visitor
 */
@RequiredArgsConstructor
@Getter
public class XMLDocument implements Serializable, JSONObjectConvertable, XMLDocumentConvertable,
        XMLStringConvertable, Visitable<XMLDocument, Visitor<XMLDocument>> {

    /**
     * A factory for creating converters between XML and JSON representations.
     */
    public static final @NotNull ConverterFactory FACTORY;

    /**
     * A converter that transforms {@code XMLDocument} instances into {@code JSONObject} instances.
     */
    public static final Converter<XMLDocument, JSONObject> CONVERTER;

    /**
     * A cache mapping Java classes to their corresponding {@link ObjectMapper} instances.
     * This facilitates efficient deserialization of XML data into Java objects.
     */
    private static final Map<Type, ObjectMapper> MAPPERS = new HashMap<>();

    /**
     * Serial version UID for serialization compatibility.
     */
    @Serial
    private static final long serialVersionUID = -8753510048552424614L;

    /**
     * The XML version of the document (e.g., 1.0).
     */
    private final Version version;

    /**
     * The encoding used in the XML document (e.g., UTF-8).
     */
    private final Encoding encoding;

    /**
     * The root element of the XML document.
     */
    private final XMLElement root;

    // Static initializer to set up the converter factory and JSON converter
    static {
        FACTORY = ConverterFactory.getFactory();
        CONVERTER = FACTORY.getXMLDocumentToJSONConverter();
    }

    /**
     * Converts the XML document to a well-formatted XML string with proper indentation and line breaks.
     *
     * @return A string representation of the XML document.
     */
    @Override
    public String toXMLString() {
        return "<?xml version=\"" + version.getValue() + "\" encoding=\"" + encoding.getValue() + "\"?>\n" + root.toXMLString();
    }

    /**
     * Converts the XML document to a compact XML string without unnecessary whitespace or line breaks.
     *
     * @return A compact string representation of the XML document.
     */
    @Override
    public String toXMLStringCompact() {
        return "<?xml version=\"" + version.getValue() + "\" encoding=\"" + encoding.getValue() + "\"?>" + root.toXMLStringCompact();
    }

    /**
     * Returns the compact string representation of the XML document.
     *
     * @return A compact string representation of the XML document.
     */
    @Override
    public String toString() {
        return toXMLStringCompact();
    }

    /**
     * Converts the XML document to a {@link JSONObject} representation.
     *
     * @return A {@code JSONObject} representing the XML document.
     */
    @Override
    public @NotNull JSONObject toJSONObject() {
        return CONVERTER.convert(this);
    }

    /**
     * Converts the XML document to a {@link JSONObject} representation, including additional metadata.
     *
     * @return A {@code JSONObject} representing the XML document with metadata.
     */
    public JSONObject toJSONObjectWithMetadata() {
        return CONVERTER.convertWithMetadata(this);
    }

    /**
     * Compares this XMLDocument to the specified object for equality.
     *
     * @param object The object to compare with.
     * @return {@code true} if the specified object is equal to this XMLDocument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof XMLDocument document)) return false;
        return version == document.version && encoding == document.encoding
                && Objects.equals(root, document.root);
    }

    /**
     * Returns a hash code value for the XMLDocument.
     *
     * @return A hash code value for this XMLDocument.
     */
    @Override
    public int hashCode() {
        return Objects.hash(version, encoding, root);
    }

    /**
     * Returns this XMLDocument instance.
     *
     * @return This XMLDocument instance.
     */
    @Override
    public XMLDocument toXMLDocument() {
        return this;
    }

    /**
     * Binds an {@link ObjectMapper} to a specific Java class type for deserialization.
     *
     * <p>Once an {@code ObjectMapper} is bound to a class, it will be used to deserialize
     * {@code XMLDocument} instances into objects of that class.</p>
     *
     * @param type   The Java class type to bind the mapper to.
     * @param mapper The {@code ObjectMapper} instance to bind.
     * @throws NullPointerException if {@code type} or {@code mapper} is {@code null}.
     */
    static void bindTo(Class<?> type, ObjectMapper mapper) {
        Objects.requireNonNull(type, "Type cannot be null");
        Objects.requireNonNull(mapper, "ObjectMapper cannot be null");
        MAPPERS.put(type, mapper);
    }

    /**
     * Binds an {@link ObjectMapper} to multiple Java class types for deserialization.
     *
     * <p>Each class in the provided array will be associated with the specified {@code ObjectMapper},
     * enabling the mapper to deserialize {@code XMLDocument} instances into objects of those classes.</p>
     *
     * @param types  An array of Java class types to bind the mapper to.
     * @param mapper The {@code ObjectMapper} instance to bind.
     * @throws NullPointerException if {@code types} or {@code mapper} is {@code null}.
     */
    static void bindTo(Class<?>[] types, ObjectMapper mapper) {
        Objects.requireNonNull(types, "Types array cannot be null");
        Objects.requireNonNull(mapper, "ObjectMapper cannot be null");
        for (Class<?> type : types) {
            bindTo(type, mapper);
        }
    }

    /**
     * Binds an {@link ObjectMapper} to multiple Java class types for deserialization.
     *
     * <p>Each class in the provided set will be associated with the specified {@code ObjectMapper},
     * enabling the mapper to deserialize {@code XMLDocument} instances into objects of those classes.</p>
     *
     * @param types  A set of Java class types to bind the mapper to.
     * @param mapper The {@code ObjectMapper} instance to bind.
     * @throws NullPointerException if {@code types} or {@code mapper} is {@code null}.
     */
    static void bindTo(@NotNull Set<Class<?>> types, @NotNull ObjectMapper mapper) {
        Objects.requireNonNull(types, "Types set cannot be null");
        Objects.requireNonNull(mapper, "ObjectMapper cannot be null");
        for (Class<?> type : types) {
            bindTo(type, mapper);
        }
    }

    /**
     * Unbinds the {@link ObjectMapper} associated with the specified Java class type.
     *
     * <p>After unbinding, the specified class will no longer have an associated mapper, and attempting
     * to deserialize {@code XMLDocument} instances into objects of that class will result in an exception.</p>
     *
     * @param type The Java class type to unbind the mapper from.
     * @throws NullPointerException if {@code type} is {@code null}.
     */
    static void unbind(@NotNull Class<?> type) {
        Objects.requireNonNull(type, "Type cannot be null");
        MAPPERS.remove(type);
    }

    /**
     * Unbinds all {@link ObjectMapper} instances from their associated Java class types.
     *
     * <p>After invoking this method, no class types will have an associated mapper, and attempting
     * to deserialize {@code XMLDocument} instances into any class will result in an exception.</p>
     */
    static void unbindAll() {
        MAPPERS.clear();
    }

    /**
     * Convenient method for deserializing this XML document into an instance of the specified Java class type.
     *
     * <p>This method utilizes the {@code ObjectMapper} bound to the specified class to perform the
     * deserialization. If no mapper is bound to the class, an {@link MappingException} is thrown.</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * Company company = xmlDocument.toObject(Company.class);
     * }</pre>
     *
     * @param type The Java class type to deserialize the XML document into.
     * @return An instance of type {@code T} representing the deserialized XML data.
     * @throws MappingException If no mapper is bound to the specified class or if deserialization fails.
     * @throws NullPointerException If {@code type} is null
     */
    public @NotNull Object toObject(@NotNull Type type) {
        ObjectMapper mapper = MAPPERS.get(type);
        if (mapper == null) {
            throw new MappingException("No mapper present for " + type);
        }

        return mapper.toObject(this, type);
    }

    /**
     * Converts this XMLDocument to an XMLDocument with additional metadata.
     *
     * <p>Currently, this method returns {@code null} and should be implemented to include metadata
     * if required.</p>
     *
     * @return An {@code XMLDocument} instance with metadata, or {@code null} if not implemented.
     */
    @Override
    public XMLDocument toXMLDocumentWithMetadata() {
        return this;
    }
}
