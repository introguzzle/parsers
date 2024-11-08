package ru.introguzzle.parsers.xml.mapping.serialization;

import lombok.experimental.ExtensionMethod;
import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.inject.Binder;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.common.utility.ReflectionUtilities;
import ru.introguzzle.parsers.config.Configuration;
import ru.introguzzle.parsers.xml.entity.XMLDocument;
import ru.introguzzle.parsers.xml.entity.XMLDocumentConvertable;
import ru.introguzzle.parsers.xml.entity.XMLElement;
import ru.introguzzle.parsers.xml.entity.annotation.XMLEntity;
import ru.introguzzle.parsers.xml.entity.annotation.XMLField;
import ru.introguzzle.parsers.xml.entity.annotation.XMLRoot;
import ru.introguzzle.parsers.xml.entity.type.XMLType;
import ru.introguzzle.parsers.xml.mapping.FieldAccessorImpl;
import ru.introguzzle.parsers.xml.mapping.XMLFieldNameConverter;
import ru.introguzzle.parsers.xml.meta.Encoding;
import ru.introguzzle.parsers.xml.meta.Version;

import java.util.Optional;

@ExtensionMethod(ReflectionUtilities.class)
public class XMLMapperImpl implements XMLMapper {
    private static final Configuration CONFIGURATION = Configuration.instance();

    private final FieldAccessor fieldAccessor = new FieldAccessorImpl();
    private final FieldNameConverter<XMLField> nameConverter = new XMLFieldNameConverter();

    private final XMLElementMapper elementMapper = new XMLElementMapperImpl(fieldAccessor, nameConverter);

    @Override
    public XMLDocument toXMLDocument(Object object) {
        if (object == null) {
            return null;
        }

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
    public XMLMapper bindTo(Class<? extends Bindable> targetType) {
        createBinder(targetType).inject(targetType);
        return this;
    }

    @Override
    public XMLMapper unbind(Class<? extends Bindable> targetType) {
        createBinder(targetType).uninject(targetType);
        return this;
    }

    @Override
    public FieldNameConverter<XMLField> getNameConverter() {
        return nameConverter;
    }

    @Override
    public FieldAccessor getFieldAccessor() {
        return fieldAccessor;
    }

    @Override
    public XMLElementMapper getElementMapper() {
        return elementMapper;
    }

    @Override
    public <T> XMLMapper withTypeHandler(XMLType type, Class<T> cls, TypeHandler<? super T> handler) {
        getElementMapper().withTypeHandler(type, cls, handler);
        return this;
    }

    @Override
    public XMLMapper clearTypeHandlers() {
        getElementMapper().clearTypeHandlers();
        return this;
    }
}
