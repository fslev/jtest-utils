package io.jtest.utils.polling;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Polling<T> {
    private static final Logger LOG = LogManager.getLogger();

    private Duration pollingDurationSec = Duration.ofSeconds(30);
    private Long pollingIntervalMillis = 3000L;
    private Double exponentialBackOff = 1.0;

    private Supplier<T> supplier = null;
    private Predicate<T> predicate = null;

    public Polling<T> duration(Long pollIntervalMillis) {
        return duration((Duration) null, pollIntervalMillis);
    }

    public Polling<T> duration(Integer pollDurationSec) {
        return duration(pollDurationSec, null);
    }

    public Polling<T> duration(Integer pollDurationSec, Long pollIntervalMillis) {
        return duration(pollDurationSec != null ? Duration.ofSeconds(pollDurationSec) : null, pollIntervalMillis);
    }

    public Polling<T> duration(Duration pollDurationSec, Long pollIntervalMillis) {
        this.pollingDurationSec = pollDurationSec != null ? pollDurationSec : this.pollingDurationSec;
        this.pollingIntervalMillis = pollIntervalMillis != null ? pollIntervalMillis : this.pollingIntervalMillis;
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
        boolean pollSucceeded = false;
        boolean pollTimeout = false;
        T result = null;
        while (!pollSucceeded && !pollTimeout) {
            long start = System.currentTimeMillis();
            result = supplier.get();
            pollSucceeded = predicate.test(result);
            if (!pollSucceeded) {
                try {
                    LOG.debug("Polling failed, I'll take another shot after {}ms", pollingIntervalMillis);
                    Thread.sleep(pollingIntervalMillis);
                    pollingIntervalMillis = (long) (pollingIntervalMillis * exponentialBackOff);
                    long elapsed = System.currentTimeMillis() - start;
                    pollingDurationSec = pollingDurationSec.minusMillis(elapsed);
                    if (pollingDurationSec.isZero() || pollingDurationSec.isNegative()) {
                        pollTimeout = true;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        LOG.debug(!pollTimeout ? "Found correct result" : "Polling timeout");
        return result;
    }
}