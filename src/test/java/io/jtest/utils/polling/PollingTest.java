package io.jtest.utils.polling;

import io.jtest.utils.exceptions.PollingTimeoutException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class PollingTest {

    @Test
    public void testPollingForResult() throws PollingTimeoutException {
        CountService service = new CountService();
        Polling<Integer> polling = new Polling<Integer>()
                .supplier(service::get)
                .duration(Duration.ofSeconds(1), 600L)
                .until(val -> val.equals(3));

        int result = polling.get();
        assertEquals(3, result);
    }

    @Test
    public void testPollingForResult_negative() {
        CountService service = new CountService();
        Polling<Integer> polling = new Polling<Integer>()
                .supplier(service::get)
                .duration(Duration.ofSeconds(1), 505L)
                .until(val -> val.equals(4));
        try {
            polling.get();
        } catch (PollingTimeoutException e) {
            return;
        }
        fail("Should fail with Polling timeout exception");
    }

    @Test
    public void testPollingForResult_negative2() {
        CountService service = new CountService();
        Polling<Integer> polling = new Polling<Integer>()
                .supplier(service::get)
                .duration(Duration.ofSeconds(1), 1001L)
                .until(val -> val.equals(3));
        try {
            polling.get();
        } catch (PollingTimeoutException e) {
            return;
        }
        fail("Should fail with Polling timeout exception");
    }

    @Test
    public void testPollingWithNullDuration() throws PollingTimeoutException {
        CountService service = new CountService();
        int result;
        Polling<Integer> polling = new Polling<Integer>()
                .supplier(service::get)
                .duration(null, 550L)
                .until(val -> val.equals(3));
        result = polling.get();
        assertEquals(3, result);
    }

    @Test
    public void testPollingExponentialBackOff() {
        CountService service = new CountService();
        Polling<Integer> polling = new Polling<Integer>()
                .supplier(service::get)
                .duration(Duration.ofSeconds(1), 405L)
                .exponentialBackOff(1.5)
                .until(val -> val.equals(5));
        try {
            polling.get();
        } catch (PollingTimeoutException e) {
            return;
        }
        fail("Should fail with Polling timeout exception");
    }

    private static class CountService {
        int count;

        public int get() {
            return ++count;
        }
    }
}
