package ru.introguzzle.parsers.xml.mapping.deserialization;

import lombok.experimental.UtilityClass;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeHandler;
import ru.introguzzle.parsers.common.util.Maps;

import java.util.Map;

@UtilityClass
public final class TypeHandlers {
    public static final Map<Class<?>, TypeHandler<?>> DEFAULT = Maps.of();
}
