package org.noear.solon.core.util;

import org.noear.solon.Utils;

import java.util.concurrent.*;

/**
 * @author noear
 * @since 1.12
 */
public class RunUtil {
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * 运行或异常
     * */
    public static void runOrThrow(RunnableEx task){
        try {
            task.run();
        } catch (Throwable e) {
            e = Utils.throwableUnwrap(e);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 并行执行
     */
    public static Future<?> parallel(Runnable task) {
        return executor.submit(task);
    }

    /**
     * 并行执行
     */
    public static <T> Future<T> parallel(Callable<T> task) {
        return executor.submit(task);
    }

    /**
     * 异步执行
     */
    public static Future<?> async(Runnable task) {
        return CompletableFuture.runAsync(task);
    }

    /**
     * 延迟执行
     */
    public static ScheduledFuture<?> delay(Runnable task, long millis) {
        return scheduledExecutor.schedule(task, millis, TimeUnit.MILLISECONDS);
    }

    /**
     * 延迟执行并重复
     */
    public static ScheduledFuture<?> delayAndRepeat(Runnable task, long millis) {
        return scheduledExecutor.scheduleWithFixedDelay(task, 1000, millis, TimeUnit.MILLISECONDS);
    }
}
