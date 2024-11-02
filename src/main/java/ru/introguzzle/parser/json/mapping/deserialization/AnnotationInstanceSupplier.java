package ru.introguzzle.parser.json.mapping.deserialization;

import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;
import ru.introguzzle.parser.common.utilities.ReflectionUtilities;
import ru.introguzzle.parser.json.entity.JSONObject;
import ru.introguzzle.parser.json.entity.annotation.JSONEntity;
import ru.introguzzle.parser.json.mapping.Fields;
import ru.introguzzle.parser.json.mapping.MappingException;
import ru.introguzzle.parser.json.mapping.MappingInstantiationException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

@ExtensionMethod({Fields.Extensions.class, ReflectionUtilities.class})
public class AnnotationInstanceSupplier implements InstanceSupplier {
    /**
     * Reference to parent mapper
     */
    private final JSONToObjectMapper mapper;

    /**
     * Method reference for forwarding recursive calls
     */
    private final BiFunction<Object, Class<?>, Object> hook;

    public AnnotationInstanceSupplier(JSONToObjectMapper m) {
        mapper = m;
        hook = m.forwardCaller();
    }

    @Override
    public <T> T get(JSONObject object, Class<T> type) {
        Optional<JSONEntity> optional = type.getAnnotationAsOptional(JSONEntity.class);
        if (optional.isEmpty() || optional.get().constructorArgs().length == 0) {
            try {
                return type.getDefaultConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new MappingInstantiationException(type);
            }
        }

        return getWithArguments(type, object, optional.get());
    }


    public <T> T getWithArguments(Class<T> type, JSONObject object, JSONEntity annotation) {
        String[] constructorNames = annotation.constructorArgs();
        List<Field> fields = Fields.getCached(type);

        Function<Field, Field> matcher = field -> {
            for (String constructorName : constructorNames) {
                if (constructorName.equals(field.getName())) {
                    return field;
                }
            }

            return null;
        };

        Class<?>[] constructorTypes = fields.stream()
                .map(matcher)
                .filter(Objects::nonNull)
                .map(Field::getType)
                .toArray(Class<?>[]::new);

        if (constructorTypes.length != constructorNames.length) {
            throw new MappingException("Invalid specified constructor arguments: " + Arrays.toString(constructorNames));
        }

        List<Field> matchedFields = fields.stream()
                .map(matcher)
                .toList();

        if (matchedFields.size() != constructorNames.length) {
            throw new MappingException("Invalid specified constructor arguments: " + Arrays.toString(constructorNames));
        }

        Object[] args = matchedFields.stream()
                .map(field -> {
                    String transformed = mapper.getNameConverter().convert(field);
                    return hook.apply(object.get(transformed), field.getType());
                })
                .toArray();

        try {
            return type.getConstructor(constructorTypes).newInstance(args);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new MappingInstantiationException(type);
        }
    }
}
