package ru.introguzzle.parsers.xml.mapping.serialization;

import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.field.MethodHandleInvoker;
import ru.introguzzle.parsers.common.field.ReadingInvoker;
import ru.introguzzle.parsers.common.inject.Binder;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.common.mapping.ClassTraverser;
import ru.introguzzle.parsers.common.mapping.Traverser;
import ru.introguzzle.parsers.common.mapping.serialization.TypeHandler;
import ru.introguzzle.parsers.common.util.ClassExtensions;
import ru.introguzzle.parsers.config.Configuration;
import ru.introguzzle.parsers.xml.entity.XMLDocument;
import ru.introguzzle.parsers.xml.entity.XMLDocumentConvertable;
import ru.introguzzle.parsers.xml.entity.XMLElement;
import ru.introguzzle.parsers.xml.entity.annotation.XMLEntity;
import ru.introguzzle.parsers.xml.entity.annotation.XMLField;
import ru.introguzzle.parsers.xml.entity.annotation.XMLRoot;
import ru.introguzzle.parsers.xml.mapping.FieldAccessorImpl;
import ru.introguzzle.parsers.xml.mapping.XMLFieldNameConverter;
import ru.introguzzle.parsers.xml.meta.Encoding;
import ru.introguzzle.parsers.xml.meta.Version;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@ExtensionMethod(ClassExtensions.class)
public class XMLMapperImpl implements XMLMapper {
    private static final Configuration CONFIGURATION = Configuration.instance();

    private final FieldAccessor fieldAccessor = new FieldAccessorImpl();
    private final Traverser<Class<?>> traverser = new ClassTraverser();
    private final ReadingInvoker readingInvoker = new MethodHandleInvoker.Reading();
    private final FieldNameConverter<XMLField> nameConverter = new XMLFieldNameConverter();

    private final XMLElementMapper elementMapper = new XMLElementMapperImpl(this);

    @Override
    public @NotNull XMLDocument toXMLDocument(@NotNull Object object) {
        Objects.requireNonNull(object);

        if (object instanceof XMLDocumentConvertable convertable) {
            return convertable.toXMLDocument();
        }

        Class<?> type = object.getClass();

        String name = type.getAnnotationAsOptional(XMLRoot.class)
                .map(XMLRoot::value)
                .orElse(CONFIGURATION.getRootName().getValue());

        Optional<XMLEntity> annotation = type.getAnnotationAsOptional(XMLEntity.class);

        Version version = annotation.map(XMLEntity::version).orElse(Version.V1_0);
        Encoding encoding = annotation.map(XMLEntity::encoding).orElse(Encoding.UTF_8);

        XMLElement root = getElementMapper().toElement(name, object);
        return new XMLDocument(version, encoding, root);
    }

    private Binder<XMLMapper, Bindable> createBinder(Class<? extends Bindable> targetType) {
        return new XMLMethodBinder(this, targetType);
    }

    @Override
    public @NotNull XMLMapper bindTo(@NotNull Class<? extends Bindable> targetType) {
        createBinder(targetType).inject(targetType);
        return this;
    }

    @Override
    public @NotNull XMLMapper unbind(@NotNull Class<? extends Bindable> targetType) {
        createBinder(targetType).uninject(targetType);
        return this;
    }

    @Override
    public @NotNull FieldNameConverter<? extends Annotation> getNameConverter() {
        return nameConverter;
    }

    @Override
    public @NotNull FieldAccessor getFieldAccessor() {
        return fieldAccessor;
    }

    @Override
    public @NotNull Traverser<Class<?>> getTraverser() {
        return traverser;
    }

    @Override
    public @NotNull ReadingInvoker getReadingInvoker() {
        return readingInvoker;
    }

    @Override
    public @NotNull XMLElementMapper getElementMapper() {
        return elementMapper;
    }

    @Override
    public <T> @NotNull XMLMapper withTypeHandler(@NotNull Class<T> type, @NotNull TypeHandler<? super T> handler) {
        getElementMapper().withTypeHandler(type, handler);
        return this;
    }

    @Override
    public @NotNull XMLMapper withTypeHandlers(@NotNull Map<Class<?>, TypeHandler<?>> handlers) {
        getElementMapper().withTypeHandlers(handlers);
        return this;
    }

    @Override
    public @NotNull XMLMapper clearTypeHandlers() {
        getElementMapper().clearTypeHandlers();
        return this;
    }
}
