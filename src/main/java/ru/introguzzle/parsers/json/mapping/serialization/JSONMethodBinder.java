package ru.introguzzle.parsers.json.mapping.serialization;

import net.bytebuddy.implementation.bind.annotation.This;
import ru.introguzzle.parsers.common.inject.AbstractBinder;
import ru.introguzzle.parsers.common.inject.BindException;
import ru.introguzzle.parsers.json.entity.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("ALL")
public class JSONMethodBinder extends AbstractBinder<JSONMapper, Bindable> {
    private static final Map<Class<?>, JSONMapper> MAPPER_REGISTRY;
    static {
        MAPPER_REGISTRY = new ConcurrentHashMap<>();
    }

    public JSONMethodBinder(JSONMapper source, Class<? extends Bindable> targetType) {
        super(source, targetType);
    }

    public static JSONObject invoke(@This Object thisObject) throws BindException {
        JSONMapper mapper = MAPPER_REGISTRY.get(thisObject.getClass());
        if (mapper == null) {
            throw new IllegalStateException("No mapper registered for class: " + thisObject.getClass());
        }

        return mapper.toJSONObject(thisObject);
    }

    @Override
    public Map<Class<?>, JSONMapper> acquireDispatcherRegistry() {
        return MAPPER_REGISTRY;
    }

    @Override
    public String getMethod() {
        return "toJSONObject";
    }
}
