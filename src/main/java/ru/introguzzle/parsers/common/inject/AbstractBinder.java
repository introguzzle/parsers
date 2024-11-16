package ru.introguzzle.parsers.common.inject;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.Map;

public abstract class AbstractBinder<S, T> implements Binder<S, T> {
    public abstract Map<Class<?>, S> getDispatchRegistry();
    private final S source;

    public AbstractBinder(S source, Class<? extends T> targetType) {
        this.source = source;
        getDispatchRegistry().put(targetType, source);
    }

    @Override
    public final void inject(String name, Class<? extends T> targetType) throws InjectException {
        try {
            ByteBuddyAgent.install();
            new ByteBuddy()
                    .redefine(targetType)
                    .method(ElementMatchers.named(name))
                    .intercept(MethodDelegation.to(getClass()))
                    .make()
                    .load(targetType.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
        } catch (Exception e) {
            throw new BindException("Binding failed. Possible reason: default method doesn't call super from interface", e);
        }
    }

    @Override
    public final void uninject(Class<? extends T> targetType) throws InjectException {
        try {
            ByteBuddyAgent.install();
            ClassReloadingStrategy strategy = ClassReloadingStrategy.fromInstalledAgent();
            ClassFileLocator locator = ClassFileLocator.ForClassLoader.of(targetType.getClassLoader());
            strategy.reset(locator, targetType);

            getDispatchRegistry().remove(targetType);
        } catch (Exception e) {
            throw new BindException("Unbinding failed.", e);
        }
    }

    @Override
    public final S getSource() {
        return source;
    }
}

