package ru.introguzzle.parsers.xml.mapping.serialization;

import ru.introguzzle.parsers.common.field.FieldAccessor;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.xml.entity.XMLElement;
import ru.introguzzle.parsers.xml.entity.type.XMLType;

import java.lang.annotation.Annotation;
import java.util.Objects;

public interface XMLElementMapper {
    record Binding<T>(XMLType type, Class<T> cls) {
        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof Binding<?> binding)) return false;
            return type == binding.type && Objects.equals(cls, binding.cls);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, cls);
        }

        public static <T> Binding<T> of(XMLType type, Class<T> cls) {
            return new Binding<>(type, cls);
        }
    }

    @SuppressWarnings("ALL")
    <T> XMLElementMapper withTypeHandler(XMLType type, Class<T> cls, TypeHandler<? super T> handler);
    XMLElementMapper clearTypeHandlers();

    XMLElement toElement(String name, Object object);

    FieldAccessor getFieldAccessor();
    FieldNameConverter<? extends Annotation> getNameConverter();
}
