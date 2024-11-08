package ru.introguzzle.parsers.json.mapping.serialization;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enum representing default type handlers, each of which is responsible
 * for serializing a specific type to a string representation.
 */
public enum DefaultTypeHandler implements TypeHandler<Object> {

    CHARACTER(Character.class, Object::toString),
    DATE(Date.class, Object::toString),
    ENUM(Enum.class, value -> ((Enum<?>) value).name()),
    TEMPORAL(Temporal.class, Object::toString),
    TEMPORAL_ADJUSTER(TemporalAdjuster.class, Object::toString),
    TEMPORAL_AMOUNT(TemporalAmount.class, Object::toString),
    UUID(UUID.class, Object::toString),
    BIG_DECIMAL(BigDecimal.class, BigDecimal::toPlainString),
    BIG_INTEGER(BigInteger.class, Object::toString),
    URL(URL.class, Object::toString),
    URI(URI.class, Object::toString),
    THROWABLE(Throwable.class, Throwable::getMessage),
    CLASS(Class.class, Class::getSimpleName);

    private final Class<?> type;
    private final TypeHandler<Object> handler;

    @SuppressWarnings("unchecked")
    <T> DefaultTypeHandler(Class<T> type, TypeHandler<? super T> handler) {
        this.type = type;
        this.handler = (TypeHandler<Object>) handler;
    }

    /**
     * Returns the handler associated with the specific type.
     *
     * @return the type handler function
     */
    @Override
    public Object apply(Object value) {
        return handler.apply(value);
    }

    /**
     * Gets the type associated with this handler.
     *
     * @return the class type handled by this enum constant
     */
    @SuppressWarnings("ALL")
    public Class<?> getType() {
        return type;
    }

    private static final Map<Class<?>, TypeHandler<?>> CACHED_MAP = new HashMap<>();

    public static Map<Class<?>, TypeHandler<?>> asMap() {
        if (CACHED_MAP.isEmpty()) {
            CACHED_MAP.putAll(Arrays.stream(values())
                    .collect(Collectors.toMap(DefaultTypeHandler::getType, t -> t)));
        }

        return CACHED_MAP;
    }
}
