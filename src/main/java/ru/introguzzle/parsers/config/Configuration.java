package ru.introguzzle.parsers.config;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.convert.NameConverter;
import ru.introguzzle.parsers.common.function.ThrowingFunction;
import ru.introguzzle.parsers.common.io.resource.ClassResourceLoader;
import ru.introguzzle.parsers.common.io.resource.ResourceLoader;
import ru.introguzzle.parsers.common.parse.BaseParser;
import ru.introguzzle.parsers.common.util.NamingUtilities;
import ru.introguzzle.parsers.yaml.SimpleYAMLParser;
import ru.introguzzle.parsers.yaml.YAMLDocument;

import java.util.Optional;
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
        converterFactoryClass = Property.ofString("converter.factory.class", "default");
        xmlNameConverter = Property.ofClass("naming.xml_converter.class", NameConverter.class, NamingUtilities::toCamelCase);
        jsonNameConverter = Property.ofClass("naming.json_converter.class", NameConverter.class, NamingUtilities::toSnakeCase);
        attributePrefix = Property.ofString("xml.attribute_prefix", "@");
        rootName = Property.ofString("xml.default_root_name", "root");
        textPlaceholder = Property.ofString("xml.text_placeholder", "@text");
        characterDataPlaceholder = Property.ofString("xml.character_data_placeholder", "@cdata");

        cacheInitialCapacity = Property.ofInteger("cache.initial_capacity", 32);
        loadFactor = Property.ofFloat("cache.load_factor", (float) Math.log(2));
        concurrencyLevel = Property.ofInteger("cache.concurrency_level", 32);
        cacheMaximalCapacity = Property.ofInteger("cache.maximal_capacity", 1000);
        cacheInitialDelay = Property.ofLong("cache.initial_delay", 1L);
        cacheInvalidatePeriod = Property.ofLong("cache.invalidate_period", 1L);
        cacheTimeUnit = Property.ofEnum("cache.time_unit", TimeUnit.class, TimeUnit.DAYS);

        debugEnabled = Property.ofBoolean("debug", false);
        entityValidationEnabled = Property.ofBoolean("entity.validation", true);
    }

    public boolean isLoaded() {
        return RESOURCE != null;
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

    private static <T> T instanceFrom(String name, Class<T> base) throws Exception {
        return base.cast(Class.forName(name).getConstructor().newInstance());
    }

    static final BaseParser<YAMLDocument> PARSER = new SimpleYAMLParser(2);
    static final ResourceLoader<YAMLDocument> FILE_LOADER = new ClassResourceLoader<>() {
        @Override
        public @NotNull BaseParser<YAMLDocument> getParser() {
            return PARSER;
        }
    };

    static final YAMLDocument RESOURCE;
    static {
        Optional<YAMLDocument> optional = FILE_LOADER.tryLoad(CONFIG_PATH);
        RESOURCE = optional.orElse(null);
    }

    public static final class Property<T> {
        private final @NotNull String key;
        private final @NotNull T value;
        private final boolean usingDefaultValue;

        private Property(@NotNull String key, @NotNull T value, boolean usingDefaultValue) {
            this.key = key;
            this.value = value;
            this.usingDefaultValue = usingDefaultValue;
        }

        static <T> Property<T> of(@NotNull String key,
                                  @NotNull ThrowingFunction<? super String, ? extends T> function,
                                  @NotNull T defaultValue) {
            try {
                String value = RESOURCE.get(key, String.class);
                T result = function.apply(value);
                return new Property<>(key, result, false);
            } catch (Throwable e) {
                return new Property<>(key, defaultValue, true);
            }
        }

        @SuppressWarnings("ALL")
        static <T> Property<T> ofClass(String key, Class<T> base, T defaultValue) {
            return of(key, className -> instanceFrom(className, base), defaultValue);
        }

        @SuppressWarnings("ALL")
        static Property<String> ofString(String key, String defaultValue) {
            return of(key, value -> value, defaultValue);
        }

        @SuppressWarnings("ALL")
        static Property<Integer> ofInteger(String key, int defaultValue) {
            return of(key, Integer::valueOf, defaultValue);
        }

        @SuppressWarnings("ALL")
        static Property<Long> ofLong(String key, long defaultValue) {
            return of(key, Long::valueOf, defaultValue);
        }

        @SuppressWarnings("ALL")
        static Property<Float> ofFloat(String key, float defaultValue) {
            return of(key, Float::valueOf, defaultValue);
        }

        @SuppressWarnings("ALL")
        static <T extends Enum<T>> Property<T> ofEnum(String key, Class<T> type, T defaultValue) {
            return of(key, value -> Enum.valueOf(type, value), defaultValue);
        }

        static Property<Boolean> ofBoolean(String key, boolean defaultValue) {
            return of(key, Boolean::valueOf, defaultValue);
        }

        public @NotNull String getKey() {
            return key;
        }

        public @NotNull T getValue() {
            return value;
        }

        @SuppressWarnings("ALL")
        public boolean isUsingDefaultValue() {
            return usingDefaultValue;
        }
    }
}
