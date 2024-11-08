package ru.introguzzle.parsers.xml.mapping.serialization;

import lombok.RequiredArgsConstructor;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.cache.CacheService;
import ru.introguzzle.parsers.common.cache.CacheSupplier;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.xml.entity.XMLAttribute;
import ru.introguzzle.parsers.xml.entity.XMLElement;
import ru.introguzzle.parsers.xml.entity.annotation.XMLField;
import ru.introguzzle.parsers.xml.entity.annotation.XMLValue;
import ru.introguzzle.parsers.xml.entity.type.XMLType;
import ru.introguzzle.parsers.xml.mapping.MappingException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static ru.introguzzle.parsers.common.mapping.ClassHierarchyTraverseUtilities.findMostSpecificMatch;

@RequiredArgsConstructor
public class XMLElementMapperImpl implements XMLElementMapper {
    private static final CacheSupplier CACHE_SUPPLIER = CacheService.getInstance();
    private static final Cache<Field, MethodHandle> GETTER_CACHE;
    private static final Cache<Binding<?>, TypeHandler<?>> TYPE_ELEMENT_HANDLER_CACHE;

    private final Map<Binding<?>, TypeHandler<?>> typeHandlers = new HashMap<>();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final FieldAccessor fieldAccessor;
    private final FieldNameConverter<XMLField> nameConverter;

    static {
        GETTER_CACHE = CACHE_SUPPLIER.newCache();
        TYPE_ELEMENT_HANDLER_CACHE = CACHE_SUPPLIER.newCache();
    }

    private <T> Map<Binding<?>, TypeHandler<?>> putHandler(XMLType type, Class<T> cls, TypeHandler<? super T> handler) {
        typeHandlers.put(Binding.of(type, cls), handler);
        return typeHandlers;
    }

    {
        putHandler(XMLType.ELEMENT, String.class, XMLElement::setText);
        putHandler(XMLType.ELEMENT, Number.class, (element, input) -> element.setText(input.toString()));
        putHandler(XMLType.ELEMENT, Character.class, (element, input) -> element.setText(input.toString()));
        putHandler(XMLType.ELEMENT, Date.class, (element, input) -> element.setText(input.toString()));
        putHandler(XMLType.ELEMENT, Enum.class, (element, input) -> element.setText(((Enum<?>) input).name()));
        putHandler(XMLType.ELEMENT, Temporal.class, (element, input) -> element.setText(input.toString()));
        putHandler(XMLType.ELEMENT, TemporalAdjuster.class, (element, input) -> element.setText(input.toString()));
        putHandler(XMLType.ELEMENT, TemporalAmount.class, (element, input) -> element.setText(input.toString()));
        putHandler(XMLType.ELEMENT, UUID.class, (element, input) -> element.setText(input.toString()));
        putHandler(XMLType.ELEMENT, BigDecimal.class, (element, input) -> element.setText(input.toPlainString()));
        putHandler(XMLType.ELEMENT, BigInteger.class, (element, input) -> element.setText(input.toString()));
        putHandler(XMLType.ELEMENT, URL.class, (element, input) -> element.setText(input.toString()));
        putHandler(XMLType.ELEMENT, URI.class, (element, input) -> element.setText(input.toString()));
        putHandler(XMLType.ELEMENT, Throwable.class, (element, input) -> element.setText(input.getMessage()));
        putHandler(XMLType.ELEMENT, Class.class, (element, input) -> element.setText(((Class<?>) input).getSimpleName()));
    }

    @SuppressWarnings("unchecked")
    private <T> TypeHandler<T> findTypeHandler(XMLType type, Class<T> cls) {
        Binding<T> binding = Binding.of(type, cls);

        return (TypeHandler<T>) TYPE_ELEMENT_HANDLER_CACHE
                .get(binding, b -> findMostSpecificTypeHandler(b.type(), b.cls()));
    }

    @SuppressWarnings("ALL")
    @Override
    public <T> XMLElementMapper withTypeHandler(XMLType type, Class<T> cls, TypeHandler<? super T> handler) {
        typeHandlers.put(Binding.of(type, cls), handler);
        return this;
    }

    @Override
    public XMLElementMapper clearTypeHandlers() {
        typeHandlers.clear();
        return this;
    }

    private TypeHandler<?> findMostSpecificTypeHandler(XMLType type, Class<?> cls) {
        Map<Class<?>, TypeHandler<?>> mapped = typeHandlers.entrySet().stream()
                .filter(entry -> entry.getKey().type() == type)
                .map(entry -> new SimpleImmutableEntry<>(entry.getKey().cls(), entry.getValue()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        return findMostSpecificMatch(mapped, cls).orElse(null);
    }

    @Override
    public XMLElement toElement(String name, Object object) {
        return toElement(name, object, null);
    }

    @SuppressWarnings("unchecked")
    public XMLElement toElement(String name, Object object, XMLType type) {
        if (object instanceof XMLElement element) {
            return element;
        }

        XMLElement root = new XMLElement(name);
        TypeHandler<?> typeHandler = findTypeHandler(type, object.getClass());
        if (typeHandler != null) {
            ((TypeHandler<Object>) typeHandler).accept(root, object);
            return root;
        }

        List<Field> fields = getFieldAccessor().acquire(object.getClass());

        int valueCount = 0;
        for (Field field : fields) {
            String fieldName = getNameConverter().apply(field);

            Object value;
            try {
                value = invokeGetter(field, object);
            } catch (Throwable e) {
                throw new MappingException("Cannot retrieve value from property: " + field.getName(), e);
            }

            XMLValue annotationValue = field.getAnnotation(XMLValue.class);
            if (annotationValue != null) {
                valueCount++;
                root.setText(value.toString());
            }

            if (valueCount > 1) {
                throw new MappingException("Ambiguous fields found for: " + object.getClass());
            }

            XMLField annotation = field.getAnnotation(XMLField.class);
            if (annotation == null) {
                continue;
            }

            type = annotation.type();

            switch (type) {
                case ELEMENT -> root.addChild(toElement(fieldName, value, type));
                case ATTRIBUTE -> root.addAttribute(new XMLAttribute(fieldName, value.toString()));
            }
        }

        return root;
    }

    private static MethodHandle acquireGetter(Field field) {
        return GETTER_CACHE.get(field, LOOKUP::unreflectGetter);
    }

    private static Object invokeGetter(Field field, Object target) {
        try {
            return acquireGetter(field).invokeWithArguments(target);
        } catch (Throwable e) {
            throw new MappingException(e);
        }
    }

    @Override
    public FieldAccessor getFieldAccessor() {
        return fieldAccessor;
    }

    @Override
    public FieldNameConverter<XMLField> getNameConverter() {
        return nameConverter;
    }
}
