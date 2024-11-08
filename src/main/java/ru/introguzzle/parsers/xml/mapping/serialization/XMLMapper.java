package ru.introguzzle.parsers.xml.mapping.serialization;

import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.xml.entity.XMLDocument;
import ru.introguzzle.parsers.xml.entity.type.XMLType;

import java.lang.annotation.Annotation;
import java.util.List;

public interface XMLMapper {
    XMLDocument toXMLDocument(Object object);
    XMLMapper bindTo(Class<? extends Bindable> targetType);
    XMLMapper unbind(Class<? extends Bindable> targetType);

    FieldNameConverter<? extends Annotation> getNameConverter();

    default XMLMapper bindTo(Class<? extends Bindable>[] targetTypes) {
        for (Class<? extends Bindable> targetType : targetTypes) {
            bindTo(targetType);
        }

        return this;
    }

    default XMLMapper bindTo(List<Class<? extends Bindable>> targetTypes) {
        for (Class<? extends Bindable> targetType : targetTypes) {
            bindTo(targetType);
        }

        return this;
    }

    FieldAccessor getFieldAccessor();
    XMLElementMapper getElementMapper();

    <T> XMLMapper withTypeHandler(XMLType type, Class<T> cls, TypeHandler<? super T> handler);
    XMLMapper clearTypeHandlers();
}
