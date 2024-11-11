package ru.introguzzle.parsers.common.mapping.serialization;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static ru.introguzzle.parsers.common.mapping.serialization.TypeHandler.newEntry;

public final class TypeHandlers {
    public static final Map<Class<?>, TypeHandler<?>> DEFAULT = Map.ofEntries(
            newEntry(Character.class, Object::toString),
            newEntry(Date.class, Object::toString),
            newEntry(Enum.class, Enum::name),
            newEntry(Temporal.class, Object::toString),
            newEntry(TemporalAdjuster.class, Object::toString),
            newEntry(TemporalAmount.class, Object::toString),
            newEntry(UUID.class, Object::toString),
            newEntry(BigDecimal.class, BigDecimal::toPlainString),
            newEntry(BigInteger.class, Object::toString),
            newEntry(URL.class, Object::toString),
            newEntry(URI.class, Object::toString),
            newEntry(Throwable.class, Throwable::getMessage),
            newEntry(Class.class, Class::getSimpleName)
    );
}
