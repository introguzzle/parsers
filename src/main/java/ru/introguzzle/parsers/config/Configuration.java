package ru.introguzzle.parsers.config;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.convert.NameConverter;
import ru.introguzzle.parsers.common.io.resource.ClassResourceLoader;
import ru.introguzzle.parsers.common.parse.BaseParser;
import ru.introguzzle.parsers.common.util.NamingUtilities;
import ru.introguzzle.parsers.yaml.SimpleYAMLParser;
import ru.introguzzle.parsers.yaml.YAMLDocument;

import java.util.concurrent.TimeUnit;

public final class Configuration {
    public static final String CONFIG_NAME = "config.yml";
    public static final String CONFIG_PATH = "ru/introguzzle/parsers/config/" + CONFIG_NAME;

    final Property<String> converterFactoryClass;
    final Property<NameConverter> xmlNameConverter;
    final Property<NameConverter> jsonNameConverter;
    final Property<String> attributePrefix;
    final Property<String> rootName;
    final Property<String> textPlaceholder;
    final Property<String> characterDataPlaceholder;

    final Property<Integer> cacheInitialCapacity;
    final Property<Float> loadFactor;
    final Property<Integer> concurrencyLevel;
    final Property<Integer> cacheMaximalCapacity;
    final Property<Long> cacheInitialDelay;
    final Property<Long> cacheInvalidatePeriod;
    final Property<TimeUnit> cacheTimeUnit;
    final Property<Boolean> debugEnabled;
    final Property<Boolean> entityValidationEnabled;

    public static Configuration instance() {
        return Holder.INSTANCE;
    }

    private static final class Holder {
        private static final Configuration INSTANCE = new Configuration();
    }

    private Configuration() {
        converterFactoryClass = FACTORY.ofString("converter.factory.class", "default");
        xmlNameConverter = FACTORY.ofClass("naming.xml_converter.class", NameConverter.class, NamingUtilities::toCamelCase);
        jsonNameConverter = FACTORY.ofClass("naming.json_converter.class", NameConverter.class, NamingUtilities::toSnakeCase);
        attributePrefix = FACTORY.ofString("xml.attribute_prefix", "@");
        rootName = FACTORY.ofString("xml.default_root_name", "root");
        textPlaceholder = FACTORY.ofString("xml.text_placeholder", "@text");
        characterDataPlaceholder = FACTORY.ofString("xml.character_data_placeholder", "@cdata");

        cacheInitialCapacity = FACTORY.ofInteger("cache.initial_capacity", 32);
        loadFactor = FACTORY.ofFloat("cache.load_factor", (float) Math.log(2));
        concurrencyLevel = FACTORY.ofInteger("cache.concurrency_level", 32);
        cacheMaximalCapacity = FACTORY.ofInteger("cache.maximal_capacity", 1000);
        cacheInitialDelay = FACTORY.ofLong("cache.initial_delay", 1L);
        cacheInvalidatePeriod = FACTORY.ofLong("cache.invalidate_period", 1L);
        cacheTimeUnit = FACTORY.ofEnum("cache.time_unit", TimeUnit.class, TimeUnit.DAYS);

        debugEnabled = FACTORY.ofBoolean("debug", false);
        entityValidationEnabled = FACTORY.ofBoolean("entity.validation", true);
    }

    public boolean isLoaded() {
        return FACTORY.isLoaded();
    }

    @SuppressWarnings("ALL")
    public Property<String> getConverterFactoryClassProperty() {
        return converterFactoryClass;
    }

    @SuppressWarnings("ALL")
    public Property<NameConverter> getXMLNameConverterProperty() {
        return xmlNameConverter;
    }

    @SuppressWarnings("ALL")
    public Property<NameConverter> getJSONNameConverterProperty() {
        return jsonNameConverter;
    }

    @SuppressWarnings("ALL")
    public Property<String> getAttributePrefixProperty() {
        return attributePrefix;
    }

    @SuppressWarnings("ALL")
    public Property<String> getRootNameProperty() {
        return rootName;
    }

    @SuppressWarnings("ALL")
    public Property<String> getTextPlaceholderProperty() {
        return textPlaceholder;
    }

    @SuppressWarnings("ALL")
    public Property<String> getCharacterDataPlaceholderProperty() {
        return characterDataPlaceholder;
    }

    @SuppressWarnings("ALL")
    public Property<Long> getCacheInvalidatePeriodProperty() {
        return cacheInvalidatePeriod;
    }

    @SuppressWarnings("ALL")
    public Property<Integer> getCacheInitialCapacityProperty() {
        return cacheInitialCapacity;
    }

    @SuppressWarnings("ALL")
    public Property<Float> getLoadFactorProperty() {
        return loadFactor;
    }

    @SuppressWarnings("ALL")
    public Property<Integer> getConcurrencyLevelProperty() {
        return concurrencyLevel;
    }

    @SuppressWarnings("ALL")
    public Property<Integer> getCacheMaximalCapacityProperty() {
        return cacheMaximalCapacity;
    }

    @SuppressWarnings("ALL")
    public Property<Long> getCacheInitialDelayProperty() {
        return cacheInitialDelay;
    }

    @SuppressWarnings("ALL")
    public Property<TimeUnit> getCacheTimeUnitProperty() {
        return cacheTimeUnit;
    }

    @SuppressWarnings("ALL")
    public Property<Boolean> getDebugEnabledProperty() {
        return debugEnabled;
    }

    @SuppressWarnings("ALL")
    public Property<Boolean> getEntityValidationEnabledProperty() {
        return entityValidationEnabled;
    }

    private static final PropertyFactory<YAMLDocument> FACTORY =
            new PropertyFactory<>(new ClassResourceLoader<>() {
                @Override
                public @NotNull BaseParser<YAMLDocument> getParser() {
                    return new SimpleYAMLParser(2);
                }
            }, CONFIG_PATH, ((document, key) -> document.get(key, String.class)));

    @Getter
    public static final class Property<T> {
        private final @NotNull String key;
        private final @NotNull T value;
        private final boolean usingDefaultValue;

        Property(@NotNull String key, @NotNull T value, boolean usingDefaultValue) {
            this.key = key;
            this.value = value;
            this.usingDefaultValue = usingDefaultValue;
        }
    }
}
