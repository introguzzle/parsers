package ru.introguzzle.parsers.common.field;

import ru.introguzzle.parsers.common.cache.Cache;
import ru.introguzzle.parsers.common.cache.CacheService;
import ru.introguzzle.parsers.common.cache.CacheSupplier;
import ru.introguzzle.parsers.common.mapping.MappingException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public final class MethodHandleInvoker {
    private static final CacheSupplier CACHE_SUPPLIER = CacheService.instance();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Cache<Field, MethodHandle> READ_CACHE = CACHE_SUPPLIER.newCache();
    private static final Cache<Field, MethodHandle> WRITE_CACHE = CACHE_SUPPLIER.newCache();

    public static final class Reading implements ReadingInvoker {
        @Override
        public Object invoke(Field field, Object instance, Object... arguments) {
            try {
                return READ_CACHE.get(field, LOOKUP::unreflectGetter).invokeWithArguments(instance);
            } catch (Throwable e) {
                throw new MappingException(e);
            }
        }

        @Override
        public Object invokeStatic(Field field, Object... arguments) {
            try {
                return READ_CACHE.get(field, LOOKUP::unreflectGetter).invokeWithArguments();
            } catch (Throwable e) {
                throw new MappingException(e);
            }
        }
    }

    public static final class Writing implements WritingInvoker {
        @Override
        public Void invoke(Field field, Object instance, Object... arguments) {
            try {
                WRITE_CACHE.get(field, LOOKUP::unreflectSetter).invokeWithArguments(instance, arguments[0]);
                return null;
            } catch (Throwable e) {
                throw new MappingException(e);
            }
        }

        @Override
        public Void invokeStatic(Field field, Object... arguments) {
            try {
                WRITE_CACHE.get(field, LOOKUP::unreflectSetter).invokeWithArguments(arguments[0]);
                return null;
            } catch (Throwable e) {
                throw new MappingException(e);
            }
        }
    }
}
