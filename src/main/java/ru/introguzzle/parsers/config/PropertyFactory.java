package ru.introguzzle.parsers.config;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.common.function.ThrowingFunction;
import ru.introguzzle.parsers.common.io.resource.ResourceLoader;

import java.lang.reflect.Field;
import java.util.function.BiFunction;

public class PropertyFactory<R> {
    private final R resource;
    private final BiFunction<R, String, String> valueAccessor;

    public PropertyFactory(ResourceLoader<R> loader, String path, BiFunction<R, String, String> valueAccessor) {
        this.resource = loader.tryLoad(path).orElse(null);
        this.valueAccessor = valueAccessor;
    }

    public boolean isLoaded() {
        return resource != null;
    }

    public <T> Configuration.Property<T> of(@NotNull String key,
                                            @NotNull ThrowingFunction<? super String, ? extends T> function,
                                            @NotNull T defaultValue) {
        try {
            String value = valueAccessor.apply(resource, key);
            T result = function.apply(value);
            return result != null
                    ? new Configuration.Property<>(key, result, false)
                    : new Configuration.Property<>(key, defaultValue, true);
        } catch (Throwable e) {
            return new Configuration.Property<>(key, defaultValue, true);
        }
    }

    private static <T> T instanceFrom(String name, Class<T> base) throws Exception {
        return base.cast(Class.forName(name).getConstructor().newInstance());
    }

    private static <T> T readStaticField(String name, Class<T> base) throws Exception {
        Field field = base.getDeclaredField(name);
        field.setAccessible(true);

        return base.cast(field.get(null));
    }

    @SuppressWarnings("ALL")
    public <T> Configuration.Property<T> ofClass(String key, Class<T> base, T defaultValue) {
        return of(key, className -> instanceFrom(className, base), defaultValue);
    }

    @SuppressWarnings("ALL")
    public <T> Configuration.Property<T> ofStaticField(String key, Class<T> base, T defaultValue) {
        return of(key, fieldName -> readStaticField(fieldName, base), defaultValue);
    }

    @SuppressWarnings("ALL")
    public Configuration.Property<String> ofString(String key, String defaultValue) {
        return of(key, value -> value, defaultValue);
    }

    @SuppressWarnings("ALL")
    public Configuration.Property<Integer> ofInteger(String key, int defaultValue) {
        return of(key, Integer::valueOf, defaultValue);
    }

    @SuppressWarnings("ALL")
    public Configuration.Property<Long> ofLong(String key, long defaultValue) {
        return of(key, Long::valueOf, defaultValue);
    }

    @SuppressWarnings("ALL")
    public Configuration.Property<Float> ofFloat(String key, float defaultValue) {
        return of(key, Float::valueOf, defaultValue);
    }

    @SuppressWarnings("ALL")
    public <T extends Enum<T>> Configuration.Property<T> ofEnum(String key, Class<T> type, T defaultValue) {
        return of(key, value -> Enum.valueOf(type, value), defaultValue);
    }

    public Configuration.Property<Boolean> ofBoolean(String key, boolean defaultValue) {
        return of(key, Boolean::valueOf, defaultValue);
    }
}
