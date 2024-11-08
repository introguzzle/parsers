package ru.introguzzle.parsers.common.cache;

import org.jetbrains.annotations.NotNull;
import ru.introguzzle.parsers.config.Configuration;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * Service class responsible for creating and managing Cache instances.
 * Ensures that all caches of this library share a single ScheduledExecutorService for periodic tasks,
 * such as cache invalidation, to optimize resource usage and maintain consistency.
 */
public final class CacheService implements ScheduledExecutorService, CacheSupplier {
    private static final Configuration CONFIGURATION;
    static {
        CONFIGURATION = Configuration.instance();
    }

    /**
     * Default initial capacity for the caches.
     * Determines the initial size of the underlying data structures.
     */
    public static final int INITIAL_CAPACITY = CONFIGURATION.getCacheInitialCapacity().getValue();

    /**
     */
    public static final float LOAD_FACTOR = CONFIGURATION.getLoadFactor().getValue();

    public static final int CONCURRENCY_LEVEL = CONFIGURATION.getConcurrencyLevel().getValue();

    /**
     * Default period between cache invalidation tasks.
     * Specifies how frequently the caches should be cleared.
     */
    public static final long INVALIDATE_PERIOD = CONFIGURATION.getCacheInvalidatePeriod().getValue();

    /**
     * Time unit for the cache invalidation period.
     * Specifies the unit of time for the PERIOD constant.
     */
    public static final TimeUnit TIME_UNIT = CONFIGURATION.getCacheTimeUnit().getValue();

    /**
     * Maximum number of entries allowed in each cache.
     * Prevents the cache from growing indefinitely and helps manage memory usage.
     */
    public static final int MAXIMAL_CAPACITY = CONFIGURATION.getCacheMaximalCapacity().getValue();

    /**
     * Default ThreadFactory provided by the Executors framework.
     * Used as a base to create custom ThreadFactories.
     */
    private static final ThreadFactory DEFAULT_FACTORY = Executors.defaultThreadFactory();

    /**
     * Custom ThreadFactory that creates daemon threads.
     * Daemon threads do not prevent the JVM from shutting down.
     * Ensures that the ScheduledExecutorService does not block application termination.
     */
    private static final ThreadFactory THREAD_FACTORY = r -> {
        Thread thread = DEFAULT_FACTORY.newThread(r);
        thread.setDaemon(true);
        return thread;
    };

    private static final CacheService INSTANCE = new CacheService(Executors.newSingleThreadScheduledExecutor(THREAD_FACTORY));

    public static CacheService getInstance() {
        return INSTANCE;
    }

    private final ScheduledExecutorService scheduler;
    private CacheService(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Creates a new Cache instance with predefined configurations.
     * All caches created through this method share this ScheduledExecutorService.
     *
     * @param <K> The type of keys maintained by this cache.
     * @param <V> The type of mapped values.
     * @return A new Cache instance configured with initial capacity, maximal capacity, and invalidation period.
     */

    @Override
    public <K, V> Cache<K, V> newCache() {
        return HashCache.HashCacheBuilder.<K, V>newBuilder()
                .setInitialCapacity(INITIAL_CAPACITY)
                .setLoadFactor(LOAD_FACTOR)
                .setConcurrencyLevel(CONCURRENCY_LEVEL)
                .setInvalidatePeriod(INVALIDATE_PERIOD, TIME_UNIT)
                .setMaximalCapacity(MAXIMAL_CAPACITY)
                .setScheduler(this)
                .build();
    }

    @NotNull
    @Override
    public ScheduledFuture<?> schedule(@NotNull Runnable command, long delay, @NotNull TimeUnit unit) {
        return scheduler.schedule(command, delay, unit);
    }

    @NotNull
    @Override
    public <V> ScheduledFuture<V> schedule(@NotNull Callable<V> callable, long delay, @NotNull TimeUnit unit) {
        return scheduler.schedule(callable, delay, unit);
    }

    @NotNull
    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(@NotNull Runnable command, long initialDelay, long period, @NotNull TimeUnit unit) {
        return scheduler.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @NotNull
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(@NotNull Runnable command, long initialDelay, long delay, @NotNull TimeUnit unit) {
        return scheduler.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    @Override
    public void shutdown() {
        scheduler.shutdown();
    }

    @NotNull
    @Override
    public List<Runnable> shutdownNow() {
        return scheduler.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return scheduler.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return scheduler.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
        return scheduler.awaitTermination(timeout, unit);
    }

    @NotNull
    @Override
    public <T> Future<T> submit(@NotNull Callable<T> task) {
        return scheduler.submit(task);
    }

    @NotNull
    @Override
    public <T> Future<T> submit(@NotNull Runnable task, T result) {
        return scheduler.submit(task, result);
    }

    @NotNull
    @Override
    public Future<?> submit(@NotNull Runnable task) {
        return scheduler.submit(task);
    }

    @NotNull
    @Override
    public <T> List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return scheduler.invokeAll(tasks);
    }

    @NotNull
    @Override
    public <T> List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks, long timeout, @NotNull TimeUnit unit) throws InterruptedException {
        return scheduler.invokeAll(tasks, timeout, unit);
    }

    @NotNull
    @Override
    public <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return scheduler.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks, long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return scheduler.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(@NotNull Runnable command) {
        scheduler.execute(command);
    }
}
