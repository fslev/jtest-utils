package io.jtest.utils.common;

import io.jtest.utils.polling.Polling;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class ActionUtils {
    private static final Logger log = LogManager.getLogger();

    public static void retryIfThrowsUp(Runnable runnable) {
        retryIfThrowsUp(runnable, Throwable.class, 30, 500);
    }

    public static <E> E retryIfThrowsUp(Supplier<E> supplier) {
        return retryIfThrowsUp(supplier, Throwable.class, 30, 500);
    }

    public static <T extends Throwable> void retryIfThrowsUp(Runnable runnable, Class<T> throwableType, int timeoutSeconds, long intervalMillis) {
        Throwable throwable = new Polling<Throwable>().supplier(() -> {
            try {
                runnable.run();
                return null;
            } catch (Throwable e) {
                if (throwableType.isInstance(e)) {
                    log.warn("Got incident. Retry action", e);
                    return e;
                } else {
                    throw e;
                }
            }
        }).until(Objects::isNull).duration(timeoutSeconds, intervalMillis).get();
        if (throwable != null) {
            throw new RuntimeException("Polling for action without incidents failed", throwable);
        }
    }

    public static <E, T extends Throwable> E retryIfThrowsUp(Supplier<E> supplier, Class<T> throwableType, int timeoutSeconds, long intervalMillis) {
        final AtomicReference<E> result = new AtomicReference<>();
        Throwable throwable = new Polling<Throwable>().supplier(() -> {
            try {
                result.set(supplier.get());
                return null;
            } catch (Throwable e) {
                if (throwableType.isInstance(e)) {
                    log.warn("Got incident. Retry action", e);
                    return e;
                } else {
                    throw e;
                }
            }
        }).until(Objects::isNull).duration(timeoutSeconds, intervalMillis).get();
        if (throwable != null) {
            throw new RuntimeException("Polling for action without incidents failed", throwable);
        }
        return result.get();
    }
}
