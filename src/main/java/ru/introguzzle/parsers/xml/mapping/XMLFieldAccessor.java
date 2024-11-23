package ru.introguzzle.parsers.xml.mapping;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.annotation.Excluded;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.field.AbstractFieldAccessor;
import ru.introguzzle.parsers.common.mapping.AnnotationData;
import ru.introguzzle.parsers.xml.entity.annotation.XMLEntity;
import ru.introguzzle.parsers.xml.entity.annotation.XMLField;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class XMLFieldAccessor extends AbstractFieldAccessor<XMLEntity, XMLField> {
    private static final Cache<Class<?>, List<Field>> CACHE;
    static {
        CACHE = CACHE_SUPPLIER.newCache();
    }

    public XMLFieldAccessor() {
        super(AnnotationData.XML);
    }

    @Override
    public @NotNull Cache<Class<?>, List<Field>> getCache() {
        return CACHE;
    }

    @Override
    public List<String> retrieveExcluded(XMLEntity annotation) {
        return Arrays.stream(annotation.excluded())
                .map(Excluded::value)
                .toList();
    }

    @Override
    public int retrieveAccessPolicy(XMLEntity annotation) {
        return annotation.accessPolicy();
    }

    @Override
    public boolean retrieveExcludeFlag(XMLField annotation) {
        return annotation.exclude();
    }
}
