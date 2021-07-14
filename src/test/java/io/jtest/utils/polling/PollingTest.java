package io.jtest.utils.polling;

import org.junit.Ignore;
import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@Ignore
public class PollingTest {

    @Test
    public void testPollerForResult() {
        int expected = 3;
        int result = new Polling<Integer>()
                .supplier(() -> generateRandomNumber(4))
                .duration(Duration.ofSeconds(10), 100L)
                .until(n -> n.equals(expected)).get();
        assertEquals(expected, result);
    }

    @Test
    public void testPollerWithNullDuration() {
        int expected = 3;
        Integer duration = null;
        int result = new Polling<Integer>()
                .supplier(() -> 3)
                .duration(duration, 100L)
                .until(n -> n.equals(expected)).get();
        assertEquals(expected, result);
    }

    @Test
    public void testPollerExponentialBackOff() {
        int expected = 3;
        int result = new Polling<Integer>()
                .supplier(() -> generateRandomNumber(4))
                .duration(Duration.ofSeconds(30), 100L)
                .exponentialBackOff(1.5)
                .until(n -> n.equals(expected)).get();
        assertEquals(expected, result);
    }

    @Test
    public void testPollerTimeout() {
        int expected = 5;
        int result = new Polling<Integer>()
                .supplier(() -> generateRandomNumber(4))
                .duration(Duration.ofSeconds(2), 100L)
                .until(n -> n.equals(expected)).get();
        assertNotEquals(expected, result);
    }

    @Test
    public void testPollerDurationTimeout() {
        int expected = 5;
        int result = new Polling<Integer>()
                .supplier(() -> generateRandomNumber(4))
                .duration(5)
                .until(n -> n.equals(expected)).get();
        assertNotEquals(expected, result);
    }

    @Test
    public void testPollerInterval() {
        int expected = 5;
        int result = new Polling<Integer>()
                .supplier(() -> generateRandomNumber(4))
                .duration(500L)
                .until(n -> n.equals(expected)).get();
        assertNotEquals(expected, result);
    }

    private int generateRandomNumber(int maxLimit) {
        return (int) (Math.random() * maxLimit);
    }
}
