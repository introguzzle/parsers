package ru.introguzzle.parsers.xml.mapping.serialization;

import net.bytebuddy.implementation.bind.annotation.This;
import ru.introguzzle.parsers.common.inject.AbstractBinder;
import ru.introguzzle.parsers.common.inject.BindException;
import ru.introguzzle.parsers.xml.entity.XMLDocument;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("ALL")
public class XMLMethodBinder extends AbstractBinder<XMLMapper, Bindable> {
    private static final Map<Class<?>, XMLMapper> MAPPER_REGISTRY;
    static {
        MAPPER_REGISTRY = new ConcurrentHashMap<>();
    }

    public XMLMethodBinder(XMLMapper source, Class<? extends Bindable> targetType) {
        super(source, targetType);
    }

    public static XMLDocument invoke(@This Object thisObject) throws BindException {
        XMLMapper mapper = MAPPER_REGISTRY.get(thisObject.getClass());
        if (mapper == null) {
            throw new IllegalStateException("No mapper registered for class: " + thisObject.getClass());
        }

        return mapper.toXMLDocument(thisObject);
    }

    @Override
    public Map<Class<?>, XMLMapper> acquireDispatcherRegistry() {
        return MAPPER_REGISTRY;
    }

    @Override
    public String getMethod() {
        return "toXMLDocument";
    }
}
