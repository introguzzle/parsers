package ru.introguzzle.parsers.xml.mapping;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.cache.CacheService;
import ru.introguzzle.parsers.common.convert.NameConverter;
import ru.introguzzle.parsers.common.field.CachingFieldNameConverter;
import ru.introguzzle.parsers.common.util.NamingUtilities;
import ru.introguzzle.parsers.xml.entity.annotation.XMLField;

import java.lang.reflect.Field;

public class XMLFieldNameConverter extends CachingFieldNameConverter<XMLField> {
    private static final Cache<Field, XMLField> CACHE = CacheService.instance().newCache();

    public XMLFieldNameConverter() {
        super(NamingUtilities::toCamelCase);
    }

    public XMLFieldNameConverter(NameConverter converter) {
        super(converter);
    }

    @Override
    public Class<XMLField> getAnnotationType() {
        return XMLField.class;
    }

    @Override
    public String retrieveDefaultValue(XMLField annotation) {
        return annotation.name();
    }

    @Override
    public @NotNull Cache<Field, XMLField> getCache() {
        return CACHE;
    }
}
