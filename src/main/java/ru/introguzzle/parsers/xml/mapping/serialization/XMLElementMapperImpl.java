package ru.introguzzle.parsers.xml.mapping.serialization;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.common.mapping.MappingException;
import ru.introguzzle.parsers.common.mapping.serialization.TypeAdapter;
import ru.introguzzle.parsers.common.util.Maps;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.cache.CacheService;
import ru.introguzzle.parsers.common.cache.CacheSupplier;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.common.field.ReadingInvoker;
import ru.introguzzle.parsers.common.mapping.Traverser;
import ru.introguzzle.parsers.common.mapping.serialization.TypeAdapters;
import ru.introguzzle.parsers.xml.entity.XMLAttribute;
import ru.introguzzle.parsers.xml.entity.XMLElement;
import ru.introguzzle.parsers.xml.entity.XMLElementConvertable;
import ru.introguzzle.parsers.xml.entity.annotation.XMLField;
import ru.introguzzle.parsers.xml.entity.annotation.XMLValue;
import ru.introguzzle.parsers.xml.entity.type.XMLType;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

@RequiredArgsConstructor
class XMLElementMapperImpl implements XMLElementMapper {
    private static final CacheSupplier CACHE_SUPPLIER = CacheService.instance();
    private static final Cache<Class<?>, TypeAdapter<?>> TYPE_HANDLER_CACHE = CACHE_SUPPLIER.newCache();
    private final Map<Class<?>, TypeAdapter<?>> typeHandlers = Maps.of(TypeAdapters.DEFAULT, Map.ofEntries(
            TypeAdapter.newEntry(Number.class, Object::toString),
            TypeAdapter.newEntry(Boolean.class, Object::toString),
            TypeAdapter.newEntry(String.class, Object::toString)
    ));

    private final XMLMapper parent;
    private final Inflector inflector = s -> {
        if (s.endsWith("s")) {
            return s.substring(0, s.length() - 1);
        }

        return s.contains("_")
                ? s.concat("_item")
                : s.concat("Item");
    };

    @SuppressWarnings("unchecked")
    private <T> TypeAdapter<T> findTypeHandler(Class<T> type) {
        return (TypeAdapter<T>) TYPE_HANDLER_CACHE.get(type, this::findMostSpecificTypeHandler);
    }

    @SuppressWarnings("ALL")
    @Override
    public <T> @NotNull XMLElementMapper withTypeAdapter(@NotNull Class<T> type, @NotNull TypeAdapter<? super T> handler) {
        typeHandlers.put(type, handler);
        return this;
    }

    @Override
    public @NotNull XMLElementMapper withTypeAdapters(@NotNull Map<Class<?>, TypeAdapter<?>> adapters) {
        typeHandlers.putAll(adapters);
        return this;
    }

    @Override
    public @NotNull XMLElementMapper clearTypeAdapters() {
        typeHandlers.clear();
        return this;
    }

    private TypeAdapter<?> findMostSpecificTypeHandler(Class<?> type) {
        return getTraverser().findMostSpecificMatch(typeHandlers, type).orElse(null);
    }

    @Override
    public @NotNull XMLElement toElement(@NotNull String name, @NotNull Object object) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(object);

        return toElement(name, object, null);
    }

    @SuppressWarnings("unchecked")
    public XMLElement toElement(@NotNull String name, @Nullable Object object, @Nullable XMLType type) {
        if (object instanceof XMLElementConvertable convertable) {
            return convertable.toXMLElement();
        }

        XMLElement root = new XMLElement(name);
        if (object == null) {
            return root;
        }

        TypeAdapter<?> typeAdapter = findTypeHandler(object.getClass());
        if (type != null && typeAdapter != null) {
            String value = ((TypeAdapter<Object>) typeAdapter).apply(object).toString();
            switch (type) {
                case ELEMENT -> root.setText(value);
                case CHARACTER_DATA -> root.setCharacterData(value);
                case ATTRIBUTE -> root.addAttribute(new XMLAttribute(name, value));
            }

            return root;
        }

        List<Field> fields = getFieldAccessor().acquire(object.getClass());

        int valueCount = 0;
        for (Field field : fields) {
            String fieldName = getNameConverter().apply(field);

            Object value;
            try {
                value = Modifier.isStatic(field.getModifiers())
                        ? getReadingInvoker().invokeStatic(field, object)
                        : getReadingInvoker().invoke(field, object);
            } catch (Throwable e) {
                throw new MappingException("Cannot retrieve value from property: " + field.getName(), e);
            }

            XMLValue annotationValue = field.getAnnotation(XMLValue.class);
            if (annotationValue != null) {
                valueCount++;
                if (valueCount > 1) {
                    throw new MappingException("Ambiguous fields found for: " + object.getClass());
                }

                root.setText(value.toString());
            }

            XMLField annotation = field.getAnnotation(XMLField.class);
            if (annotation == null) {
                continue;
            }

            type = annotation.type();
            switch (type) {
                case ELEMENT -> root.addChild(handle(annotation, fieldName, value));
                case ATTRIBUTE -> root.addAttribute(new XMLAttribute(fieldName, value.toString()));
            }
        }

        return root;
    }

    private XMLElement handle(XMLField parentAnnotation, String name, Object value) {
        if (value == null) {
            return new XMLElement(name);
        }

        Class<?> type = value.getClass();
        if (Iterable.class.isAssignableFrom(type) || type.isArray()) {
            XMLElement root = new XMLElement(name);
            String elementName = parentAnnotation.element();
            if (elementName == null || elementName.isEmpty()) {
                elementName = inflector.singularize(name);
            }

            if (Iterable.class.isAssignableFrom(type)) {
                for (Object item : (Iterable<?>) value) {
                    root.addChild(handle(null, elementName, item));
                }
            }

            if (type.isArray()) {
                Class<?> componentType = type.getComponentType();
                // Wrap primitives
                if (componentType.isPrimitive()) {
                    int length = Array.getLength(value);
                    for (int i = 0; i < length; i++) {
                        Object item = Array.get(value, i);
                        root.addChild(handle(null, elementName, item));
                    }

                    return root;
                }

                for (Object item : (Object[]) value) {
                    root.addChild(handle(null, elementName, item));
                }
            }

            return root;
        }

        return toElement(name, value, XMLType.ELEMENT);
    }

    @Override
    public @NotNull FieldAccessor getFieldAccessor() {
        return parent.getFieldAccessor();
    }

    @Override
    public @NotNull Traverser<Class<?>> getTraverser() {
        return parent.getTraverser();
    }

    @Override
    public @NotNull ReadingInvoker getReadingInvoker() {
        return parent.getReadingInvoker();
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull FieldNameConverter<XMLField> getNameConverter() {
        return (FieldNameConverter<XMLField>) parent.getNameConverter();
    }
}
