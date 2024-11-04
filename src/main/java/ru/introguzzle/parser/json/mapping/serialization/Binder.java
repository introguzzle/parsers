package ru.introguzzle.parser.json.mapping.serialization;

import ru.introguzzle.parser.common.inject.MethodInjector;

public interface Binder extends MethodInjector<JSONMapper, Bindable> {
    String METHOD = "toJSONObject";

    @Override
    void inject(String name, Class<? extends Bindable> target) throws BindException;

    @SuppressWarnings("ALL")
    default Binder inject(Class<? extends Bindable> type) throws BindException {
        inject(METHOD, type);
        return this;
    }

    @Override
    JSONMapper getSource();
}
