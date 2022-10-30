package io.jtest.utils.readme;

import io.jtest.utils.exceptions.PollingTimeoutException;
import io.jtest.utils.polling.Polling;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PollingTests {

    @Test
    public void testPolling() throws PollingTimeoutException {
        Integer result = new Polling<Integer>()
                .duration(Duration.ofSeconds(30), 5L)
                .exponentialBackOff(1.0)
                .supplier(() -> generateRandomFromInterval(4, 7))
                .until(number -> number == 6).get();
        assertEquals(6, result);
    }

    private static int generateRandomFromInterval(int start, int end) {
        return (int) (start + (end - start) * Math.random());
    }
}
