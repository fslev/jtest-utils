package io.jtest.utils.common;

import io.jtest.utils.polling.Polling;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Polling for supplier result until no exception is thrown or timeout is reached
 */
public class SupplierUtils {
    private static final Logger LOG = LogManager.getLogger();

    public static <E> E pollIfThrows(Supplier<E> supplier) {
        return pollIfThrows(supplier, Throwable.class, 30, 500, 1.0);
    }

    public static <E, T extends Throwable> E pollIfThrows(Supplier<E> supplier, Class<T> throwableType, int timeoutSeconds,
                                                          long intervalMillis, double exponentialBackOff) {
        final AtomicReference<E> result = new AtomicReference<>();
        Throwable throwable = new Polling<Throwable>().until(Objects::isNull)
                .duration(timeoutSeconds, intervalMillis)
                .exponentialBackOff(exponentialBackOff)
                .supplier(() -> {
                    try {
                        result.set(supplier.get());
                        return null;
                    } catch (Throwable e) {
                        if (throwableType.isInstance(e)) {
                            LOG.warn("Got incident. Retry action", e);
                            return e;
                        } else {
                            throw e;
                        }
                    }
                }).get();
        if (throwable != null) {
            throw new RuntimeException("Polling for action without incidents failed", throwable);
        }
        return result.get();
    }
}
