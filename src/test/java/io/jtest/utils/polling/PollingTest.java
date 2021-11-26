package io.jtest.utils.polling;

import io.jtest.utils.exceptions.PollingTimeoutException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class PollingTest {

    @Test
    public void testPollerForResult() throws PollingTimeoutException {
        Service service = new Service(2);
        Polling<Boolean> polling = new Polling<Boolean>()
                .supplier(service::get)
                .duration(Duration.ofSeconds(1), 600L)
                .until(val -> val.equals(true));
        boolean result = polling.get();
        assertTrue(result);
        assertTrue(polling.getLastResult());
    }

    @Test
    public void testPollerForResult_negative() {
        Service service = new Service(4);
        Polling<Boolean> polling = new Polling<Boolean>()
                .supplier(service::get)
                .duration(Duration.ofSeconds(1), 505L)
                .until(val -> val.equals(true));
        try {
            polling.get();
        } catch (PollingTimeoutException e) {
            assertFalse(polling.getLastResult());
            return;
        }
        fail("Should fail with Polling timeout exception");
    }

    @Test
    public void testPollerForResult_negative2() {
        Service service = new Service(3);
        Polling<Boolean> polling = new Polling<Boolean>()
                .supplier(service::get)
                .duration(Duration.ofSeconds(1), 1001L)
                .until(val -> val.equals(true));
        try {
            polling.get();
        } catch (PollingTimeoutException e) {
            assertFalse(polling.getLastResult());
            return;
        }
        fail("Should fail with Polling timeout exception");
    }

    @Test
    public void testPollerWithNullDuration() throws PollingTimeoutException {
        Service service = new Service(2);
        boolean result;
        Polling<Boolean> polling = new Polling<Boolean>()
                .supplier(service::get)
                .duration((Duration) null, 550L)
                .until(val -> val.equals(true));
        result = polling.get();
        assertTrue(result);
        assertTrue(polling.getLastResult());
    }

    @Test
    public void testPollerExponentialBackOff() {
        Service service = new Service(4);
        Polling<Boolean> polling = new Polling<Boolean>()
                .supplier(service::get)
                .duration(Duration.ofSeconds(1), 405L)
                .exponentialBackOff(1.5)
                .until(val -> val.equals(true));
        try {
            polling.get();
        } catch (PollingTimeoutException e) {
            assertFalse(polling.getLastResult());
            return;
        }
        fail("Should fail with Polling timeout exception");
    }

    @Test
    public void testPollerTimeout() {
        Service service = new Service(1000);
        Polling<Boolean> polling = new Polling<Boolean>()
                .supplier(service::get)
                .duration(Duration.ofSeconds(1), 100L)
                .until(val -> val.equals(true));
        try {
            polling.get();
        } catch (PollingTimeoutException e) {
            assertFalse(polling.getLastResult());
            return;
        }
        fail("Should fail with Polling timeout exception");
    }

    private static class Service {
        int successfulRetry;
        int retry;

        public Service(int successAfterRetry) {
            this.successfulRetry = successAfterRetry;
        }

        public boolean get() {
            return ++retry == successfulRetry;
        }
    }
}
