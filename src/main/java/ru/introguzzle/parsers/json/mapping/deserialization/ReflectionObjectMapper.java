package ru.introguzzle.parsers.json.mapping.deserialization;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.field.ReflectionInvoker;
import ru.introguzzle.parsers.common.field.WritingInvoker;
import ru.introguzzle.parsers.common.function.TriConsumer;
import ru.introguzzle.parsers.common.mapping.MappingException;
import ru.introguzzle.parsers.common.mapping.deserialization.InstanceSupplier;
import ru.introguzzle.parsers.common.util.DelegatingMap;
import ru.introguzzle.parsers.json.entity.JSONObject;
import ru.introguzzle.parsers.json.entity.annotation.JSONEntity;
import ru.introguzzle.parsers.json.entity.annotation.JSONField;
import ru.introguzzle.parsers.common.field.FieldNameConverter;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.function.BiFunction;

@RequiredArgsConstructor
class ReflectionObjectMapper extends AbstractObjectMapper {
    private final FieldNameConverter<JSONField> nameConverter;

    private final WritingInvoker writingInvoker = new ReflectionInvoker.Writing();
    private final InstanceSupplier<JSONObject> instanceSupplier = InstanceSupplier.getReflectionSupplier(
            this, JSONEntity.class, JSONField.class, JSONEntity::constructorArguments, DelegatingMap::get);

    @Override
    protected @NotNull String getCircularPlaceholder() {
        return "<CIRCULAR_REFERENCE>";
    }

    @Override
    public @NotNull InstanceSupplier<JSONObject> getInstanceSupplier() {
        return instanceSupplier;
    }

    @Override
    protected @NotNull BiFunction<Type, Integer, Object> getArraySupplier() {
        return (type, size) -> {
            try {
                return Array.newInstance(getTypeResolver().getRawType(type), size);
            } catch (NegativeArraySizeException e) {
                throw new MappingException("Can't instantiate array");
            }
        };
    }

    @Override
    protected @NotNull TriConsumer<Object, Integer, Object> getArraySetter() {
        return Array::set;
    }

    @Override
    public @NotNull FieldNameConverter<JSONField> getNameConverter() {
        return nameConverter;
    }

    @Override
    public @NotNull WritingInvoker getWritingInvoker() {
        return writingInvoker;
    }
}
