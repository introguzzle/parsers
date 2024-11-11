package ru.introguzzle.parsers.xml.mapping.serialization;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parsers.common.Maps;
import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.cache.CacheService;
import ru.introguzzle.parsers.common.cache.CacheSupplier;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.common.field.ReadingInvoker;
import ru.introguzzle.parsers.common.mapping.Traverser;
import ru.introguzzle.parsers.common.mapping.serialization.TypeHandler;
import ru.introguzzle.parsers.common.mapping.serialization.TypeHandlers;
import ru.introguzzle.parsers.xml.entity.XMLAttribute;
import ru.introguzzle.parsers.xml.entity.XMLElement;
import ru.introguzzle.parsers.xml.entity.annotation.XMLField;
import ru.introguzzle.parsers.xml.entity.annotation.XMLValue;
import ru.introguzzle.parsers.xml.entity.type.XMLType;
import ru.introguzzle.parsers.xml.mapping.XMLMappingException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

@RequiredArgsConstructor
public class XMLElementMapperImpl implements XMLElementMapper {
    private static final CacheSupplier CACHE_SUPPLIER = CacheService.instance();
    private static final Cache<Class<?>, TypeHandler<?>> TYPE_HANDLER_CACHE = CACHE_SUPPLIER.newCache();
    private final Map<Class<?>, TypeHandler<?>> typeHandlers = Maps.of(TypeHandlers.DEFAULT, Map.ofEntries(
            TypeHandler.newEntry(Number.class, Object::toString),
            TypeHandler.newEntry(Boolean.class, Object::toString),
            TypeHandler.newEntry(String.class, Object::toString)
    ));

    private final XMLMapper parent;
    private final Inflector inflector = new DefaultInflector();

    @SuppressWarnings("unchecked")
    private <T> TypeHandler<T> findTypeHandler(Class<T> type) {
        return (TypeHandler<T>) TYPE_HANDLER_CACHE.get(type, this::findMostSpecificTypeHandler);
    }

    @SuppressWarnings("ALL")
    @Override
    public <T> @NotNull XMLElementMapper withTypeHandler(@NotNull Class<T> type, @NotNull TypeHandler<? super T> handler) {
        typeHandlers.put(type, handler);
        return this;
    }

    @Override
    public @NotNull XMLElementMapper withTypeHandlers(@NotNull Map<Class<?>, TypeHandler<?>> handlers) {
        typeHandlers.putAll(handlers);
        return this;
    }

    @Override
    public @NotNull XMLElementMapper clearTypeHandlers() {
        typeHandlers.clear();
        return this;
    }

    @Override
    public @NotNull XMLElementMapper bindTo(@NotNull Class<? extends Bindable> targetType) {
        parent.bindTo(targetType);
        return this;
    }

    @Override
    public @NotNull XMLElementMapper unbind(@NotNull Class<? extends Bindable> targetType) {
        parent.unbind(targetType);
        return this;
    }

    private TypeHandler<?> findMostSpecificTypeHandler(Class<?> type) {
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
        if (object instanceof XMLElement element) {
            return element;
        }

        XMLElement root = new XMLElement(name);
        if (object == null) {
            return root;
        }

        TypeHandler<?> typeHandler = findTypeHandler(object.getClass());
        if (type != null && typeHandler != null) {
            String value = ((TypeHandler<Object>) typeHandler).apply(object).toString();
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
                value = getReadingInvoker().invoke(field, object);
            } catch (Throwable e) {
                throw new XMLMappingException("Cannot retrieve value from property: " + field.getName(), e);
            }

            XMLValue annotationValue = field.getAnnotation(XMLValue.class);
            if (annotationValue != null) {
                valueCount++;
                if (valueCount > 1) {
                    throw new XMLMappingException("Ambiguous fields found for: " + object.getClass());
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
