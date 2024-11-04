package ru.introguzzle.parser.json.mapping.serialization;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;
import ru.introguzzle.parser.json.entity.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("ALL")
public class ByteBinder implements Binder {
    private static final Map<Class<?>, JSONMapper> MAPPER_REGISTRY;
    static {
        MAPPER_REGISTRY = new ConcurrentHashMap<>();
    }

    private final JSONMapper source;

    public ByteBinder(JSONMapper source, Class<? extends Bindable> targetType) {
        this.source = source;
        MAPPER_REGISTRY.put(targetType, source);
    }

    public static JSONObject invoke(@This Object thisObject) throws BindException {
        JSONMapper mapper = MAPPER_REGISTRY.get(thisObject.getClass());
        if (mapper == null) {
            throw new IllegalStateException("No mapper registered for class: " + thisObject.getClass());
        }

        return mapper.toJSONObject(thisObject);
    }

    @Override
    public final void inject(String name, Class<? extends Bindable> targetType) {
        try {
            ByteBuddyAgent.install();
            new ByteBuddy()
                    .redefine(targetType)
                    .method(ElementMatchers.named(name))
                    .intercept(MethodDelegation.to(ByteBinder.class))
                    .make()
                    .load(targetType.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
        } catch (Exception e) {
            throw new BindException("Binding failed. Possible reason: default method doesn't call super from interface", e);
        }
    }

    @Override
    public final void uninject(Class<? extends Bindable> targetType) throws BindException {
        try {
            ByteBuddyAgent.install();
            ClassReloadingStrategy strategy = ClassReloadingStrategy.fromInstalledAgent();
            ClassFileLocator locator = ClassFileLocator.ForClassLoader.of(targetType.getClassLoader());
            strategy.reset(locator, targetType);

            MAPPER_REGISTRY.remove(targetType);
        } catch (Exception e) {
            throw new BindException("Unbinding failed.", e);
        }
    }

    @Override
    public final JSONMapper getSource() {
        return source;
    }
}
