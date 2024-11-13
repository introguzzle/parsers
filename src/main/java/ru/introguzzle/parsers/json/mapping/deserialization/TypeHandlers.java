package ru.introguzzle.parsers.json.mapping.deserialization;

import lombok.experimental.UtilityClass;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeHandler;
import ru.introguzzle.parsers.json.mapping.JSONMappingException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@UtilityClass
public final class TypeHandlers {
    public static final Map<Class<?>, TypeHandler<?>> DEFAULT = Map.ofEntries(
            TypeHandler.newEntry(Date.class, (o, genericTypes) -> {
                if (o instanceof String string) {
                    return Date.from(Instant.parse(string));
                }

                throw new JSONMappingException(Date.class, o.getClass());
            }),

            TypeHandler.newEntry(BigDecimal.class, (o, genericTypes) -> {
                if (o instanceof String string) {
                    return new BigDecimal(string);
                }

                throw new JSONMappingException(BigDecimal.class, o.getClass());
            }),

            TypeHandler.newEntry(BigInteger.class, (o, genericTypes) -> {
                if (o instanceof String string) {
                    return new BigInteger(string);
                }

                throw new JSONMappingException(BigInteger.class, o.getClass());
            }),

            TypeHandler.newEntry(URI.class, (o, genericTypes) -> {
                if (o instanceof String string) {
                    return URI.create(string);
                }

                throw new JSONMappingException(URI.class, o.getClass());
            }),

            TypeHandler.newEntry(Throwable.class, (o, genericTypes) -> {
                if (o instanceof String string) {
                    return new Throwable(string);
                }

                throw new JSONMappingException(Throwable.class, o.getClass());
            }),

            TypeHandler.newEntry(Class.class, (o, genericTypes) -> {
                if (o instanceof String string) {
                    try {
                        return Class.forName(string);
                    } catch (ClassNotFoundException e) {
                        throw new JSONMappingException("Cannot instantiate Class", e);
                    }
                }

                throw new JSONMappingException(Class.class, o.getClass());
            })
    );
}
