package ru.introguzzle.parser.json.mapping.deserialization;

import ru.introguzzle.parser.common.utilities.NamingUtilities;
import ru.introguzzle.parser.json.mapping.FieldNameConverter;
import ru.introguzzle.parser.json.mapping.MappingInstantiationException;
import ru.introguzzle.parser.json.mapping.ReflectionFieldNameConverter;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.function.BiFunction;

public class ReflectionObjectMapper extends AbstractObjectMapper {
    private final InstanceSupplier instanceSupplier = new AnnotationInstanceSupplier(this);
    private final FieldNameConverter<Field> nameConverter = new ReflectionFieldNameConverter(NamingUtilities::toSnakeCase);

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
    public FieldNameConverter<Field> getNameConverter() {
        return nameConverter;
    }
}
