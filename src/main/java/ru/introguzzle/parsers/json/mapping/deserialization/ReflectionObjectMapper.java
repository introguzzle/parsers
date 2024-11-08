package ru.introguzzle.parsers.json.mapping.deserialization;

import ru.introguzzle.parsers.json.entity.annotation.JSONField;
import ru.introguzzle.parsers.common.field.FieldNameConverter;
import ru.introguzzle.parsers.json.mapping.MappingInstantiationException;
import ru.introguzzle.parsers.json.mapping.JSONFieldNameConverter;

import java.lang.reflect.Array;
import java.util.function.BiFunction;

public class ReflectionObjectMapper extends AbstractObjectMapper {
    private final InstanceSupplier instanceSupplier = new AnnotationInstanceSupplier(this);
    private final FieldNameConverter<JSONField> nameConverter = new JSONFieldNameConverter();

    @Override
    protected String getCircularPlaceholder() {
        return "<CIRCULAR_REFERENCE>";
    }

    @Override
    protected InstanceSupplier getInstanceSupplier() {
        return instanceSupplier;
    }

    @Override
    protected BiFunction<Class<?>, Integer, Object> getArraySupplier() {
        return (type, size) -> {
            try {
                return Array.newInstance(type, size);
            } catch (NegativeArraySizeException e) {
                throw new MappingInstantiationException("Can't instantiate array");
            }
        };
    }

    @Override
    protected TriConsumer<Object, Integer, Object> getArraySetter() {
        return Array::set;
    }

    @Override
    public FieldNameConverter<JSONField> getNameConverter() {
        return nameConverter;
    }
}
