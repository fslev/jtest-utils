package io.jtest.utils.polling;

import io.jtest.utils.exceptions.PollingTimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Polling<T> {
    private static final Logger LOG = LogManager.getLogger();

    private Duration pollingDuration = Duration.ofSeconds(30);
    private Long pollingIntervalMillis = 3000L;
    private Double exponentialBackOff = 1.0;

    private Supplier<T> supplier;
    private Predicate<T> until;

    private T result;

    public Polling<T> duration(Duration pollingDuration, Long pollingIntervalMillis) {
        this.pollingDuration = pollingDuration != null ? pollingDuration : this.pollingDuration;
        this.pollingIntervalMillis = pollingIntervalMillis != null ? pollingIntervalMillis : this.pollingIntervalMillis;
        return this;
    }

    public Polling<T> exponentialBackOff(Double exp) {
        if (exp != null) {
            this.exponentialBackOff = exp;
        }
        return this;
    }

    public Polling<T> supplier(Supplier<T> supplier) {
        this.supplier = supplier;
        return this;
    }

    public Polling<T> until(Predicate<T> predicate) {
        this.until = predicate;
        return this;
    }

    public T getLastResult() {
        return result;
    }

    public T get() throws PollingTimeoutException {
        LOG.debug("Polling for result...");
        boolean success = false;
        long interval = pollingIntervalMillis;
        long start = System.currentTimeMillis();
        while (!success) {
            result = supplier.get();
            success = until.test(result);
            if (!success) {
                try {
                    long elapsed = System.currentTimeMillis() - start;
                    if (pollingDuration.minusMillis(elapsed).toMillis() <= 0) {
                        throw new PollingTimeoutException();
                    }
                    LOG.debug("Polling failed, I'll take another shot after {}ms", interval);
                    Thread.sleep(interval);
                    interval = (long) (interval * exponentialBackOff);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return result;
    }
}