package io.jtest.utils.polling;

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

    private Supplier<T> supplier = null;
    private Predicate<T> predicate = null;

    public Polling<T> duration(Long pollingIntervalMillis) {
        return duration((Duration) null, pollingIntervalMillis);
    }

    public Polling<T> duration(Integer pollingDurationSec) {
        return duration(pollingDurationSec, null);
    }

    public Polling<T> duration(Integer pollingDurationSec, Long pollingIntervalMillis) {
        return duration(pollingDurationSec != null ? Duration.ofSeconds(pollingDurationSec) : null, pollingIntervalMillis);
    }

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
        this.predicate = predicate;
        return this;
    }

    public T get() {
        LOG.debug("Polling for result...");
        boolean success = false;
        boolean timeout = false;
        T result = null;
        long interval = pollingIntervalMillis;
        long start = System.currentTimeMillis();
        while (!success && !timeout) {
            result = supplier.get();
            success = predicate.test(result);
            if (!success) {
                try {
                    LOG.debug("Polling failed, I'll take another shot after {}ms", interval);
                    Thread.sleep(interval);
                    interval = (long) (interval * exponentialBackOff);
                    long elapsed = System.currentTimeMillis() - start;
                    if (pollingDuration.minusMillis(elapsed).toMillis() <= 0) {
                        timeout = true;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        LOG.debug(!timeout ? "Found correct result" : "Polling timeout");
        return result;
    }
}