package io.jtest.utils.polling;

import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PollingTest {

    @Test
    public void testPollerForResult() {
        Service service = new Service(3);
        boolean result = new Polling<Boolean>()
                .supplier(service::get)
                .duration(Duration.ofSeconds(1), 410L)
                .until(val -> val.equals(true)).get();
        assertTrue(result);
    }

    @Test
    public void testPollerForResult_negative() {
        Service service = new Service(3);
        boolean result = new Polling<Boolean>()
                .supplier(service::get)
                .duration(Duration.ofSeconds(1), 550L)
                .until(val -> val.equals(true)).get();
        assertFalse(result);
    }

    @Test
    public void testPollerWithNullDuration() {
        Service service = new Service(1);
        boolean result = new Polling<Boolean>()
                .supplier(service::get)
                .duration((Duration) null, 550L)
                .until(val -> val.equals(true)).get();
        assertTrue(result);
    }

    @Test
    public void testPollerExponentialBackOff() {
        Service service = new Service(3);
        boolean result = new Polling<Boolean>()
                .supplier(service::get)
                .duration(Duration.ofSeconds(1), 410L)
                .exponentialBackOff(1.5)
                .until(val -> val.equals(true)).get();
        assertFalse(result);
    }

    @Test
    public void testPollerTimeout() {
        Service service = new Service(1000);
        boolean result = new Polling<Boolean>()
                .supplier(service::get)
                .duration(Duration.ofSeconds(1), 400L)
                .until(val -> val.equals(true)).get();
        assertFalse(result);
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
