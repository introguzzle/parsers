package ru.introguzzle.parsers.json.mapping.deserialization;

import lombok.experimental.UtilityClass;
import ru.introguzzle.parsers.common.mapping.MappingException;
import ru.introguzzle.parsers.common.mapping.deserialization.TypeAdapter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@UtilityClass
public final class TypeHandlers {
    public static final Map<Class<?>, TypeAdapter<?>> DEFAULT = Map.ofEntries(
            TypeAdapter.newEntry(Date.class, (o, genericTypes) -> {
                if (o instanceof String string) {
                    return Date.from(Instant.parse(string));
                }

                throw MappingException.ofConversion(Date.class, o.getClass());
            }),

            TypeAdapter.newEntry(BigDecimal.class, (o, genericTypes) -> {
                if (o instanceof String string) {
                    return new BigDecimal(string);
                }

                throw MappingException.ofConversion(BigDecimal.class, o.getClass());
            }),

            TypeAdapter.newEntry(BigInteger.class, (o, genericTypes) -> {
                if (o instanceof String string) {
                    return new BigInteger(string);
                }

                throw MappingException.ofConversion(BigInteger.class, o.getClass());
            }),

            TypeAdapter.newEntry(URI.class, (o, genericTypes) -> {
                if (o instanceof String string) {
                    return URI.create(string);
                }

                throw MappingException.ofConversion(URI.class, o.getClass());
            }),

            TypeAdapter.newEntry(Throwable.class, (o, genericTypes) -> {
                if (o instanceof String string) {
                    return new Throwable(string);
                }

                throw MappingException.ofConversion(Throwable.class, o.getClass());
            }),

            TypeAdapter.newEntry(Class.class, (o, genericTypes) -> {
                if (o instanceof String string) {
                    try {
                        return Class.forName(string);
                    } catch (ClassNotFoundException e) {
                        throw MappingException.ofInstantiation(Class.class, e);
                    }
                }

                throw MappingException.ofConversion(Class.class, o.getClass());
            })
    );
}
