package ru.introguzzle.parser.common.convert;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parser.common.io.config.ConfigFactory;
import ru.introguzzle.parser.common.utilities.NamingUtilities;
import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.xml.XMLDocument;
import ru.introguzzle.parser.xml.XMLElement;
import ru.introguzzle.parser.yaml.YAMLDocument;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public abstract class ConverterFactory {
    public static final String CONFIG_NAME = "config.yml";
    public static final String CONFIG_PATH = "ru/introguzzle/parser/config/" + CONFIG_NAME;

    private static <T> T instanceFrom(String name, Class<T> base) throws Exception {
        return base.cast(Class.forName(name).getConstructor().newInstance());
    }

    public record Property<T>(@NotNull String key, @NotNull T value) {
        private interface Function<T, R> {
            R apply(T t) throws Exception;
        }

        static <T> Property<T> of(@NotNull String key,
                                  @NotNull Function<? super String, ? extends T> function,
                                  @NotNull T defaultValue) {
            try {
                String value = CONFIG.get(key, String.class);
                T result = function.apply(value);
                return new Property<>(key, result);

            } catch (Exception e) {
                return new Property<>(key, defaultValue);
            }
        }

        static <T> Property<T> ofClass(String key, Class<T> base, T defaultValue) {
            return of(key, className -> instanceFrom(className, base), defaultValue);
        }

        static Property<String> ofString(String key, String defaultValue) {
            return of(key, value -> value, defaultValue);
        }
    }

    static final ConfigFactory CONFIG_FACTORY = ConfigFactory.getInstance();
    static final YAMLDocument CONFIG;
    static {
        Optional<YAMLDocument> optional = CONFIG_FACTORY.tryLoadConfig(CONFIG_PATH);
        CONFIG = optional.orElse(null);
    }

    public static final Property<NameConverter> DEFAULT_TO_XML_NAME_CONVERTER;

    static {
        DEFAULT_TO_XML_NAME_CONVERTER = Property.ofClass(
                "naming.xml_converter.class",
                NameConverter.class,
                NamingUtilities::toCamelCase
        );
    }

    public static final Property<NameConverter> DEFAULT_TO_JSON_NAME_CONVERTER;

    static {
        DEFAULT_TO_JSON_NAME_CONVERTER = Property.ofClass(
                "naming.json_converter.class",
                NameConverter.class,
                NamingUtilities::toSnakeCase
        );
    }

    public static final Property<String> DEFAULT_ATTRIBUTE_PREFIX = Property.ofString(
            "xml.attribute_prefix",
            "@"
    );

    public static final Property<String> DEFAULT_ROOT_NAME = Property.ofString(
            "xml.default_root_name",
            "root"
    );

    public static final Property<String> DEFAULT_TEXT_PLACEHOLDER = Property.ofString(
            "xml.text_placeholder",
            "@text"
    );

    public static final Property<String> DEFAULT_CHARACTER_DATA_PLACEHOLDER = Property.ofString(
            "xml.character_data_placeholder",
            "@cdata"
    );

    public static @NotNull ConverterFactory getFactory() {
        if (CONFIG == null) {
            return getDefault();
        }

        String setting = CONFIG.get("converter.factory.class", String.class);
        if (setting == null) {
            return getDefault();
        }

        try {
            Class<?> cls = Class.forName(setting);
            if (ConverterFactory.class.isAssignableFrom(cls)) {
                return (ConverterFactory) cls.getConstructor().newInstance();
            } else {
                throw new ClassCastException(setting + " is not an instance of ConverterFactory");
            }
        } catch (ClassNotFoundException | InstantiationException |
                 InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Error loading ConverterFactory class: " + setting, e);
        }
    }

    private static @NotNull ConverterFactory getDefault() {
        return new ConverterFactoryImpl(
                DEFAULT_TO_XML_NAME_CONVERTER.value,
                DEFAULT_TO_JSON_NAME_CONVERTER.value,
                DEFAULT_ATTRIBUTE_PREFIX.value,
                DEFAULT_ROOT_NAME.value,
                DEFAULT_TEXT_PLACEHOLDER.value,
                DEFAULT_CHARACTER_DATA_PLACEHOLDER.value
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
