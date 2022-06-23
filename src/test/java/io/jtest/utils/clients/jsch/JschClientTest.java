package io.jtest.utils.clients.jsch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class JschClientTest {

    @Test
    public void testInvalidSshConnection() {
        Exception e = assertThrows(RuntimeException.class, () ->
                new JschClient("localhost", 22, "testus", "testpw", "", null));
        assertTrue(e.getMessage().contains("FileNotFoundException"));
    }
}
