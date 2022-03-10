package io.jtest.utils.clients.shell;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
public class ShellClientTest {

    @Test
    public void testSimpleShellCommand() {
        ShellClient client = new ShellClient();
        assertTrue(client.execute("bash", "-c", "ls -alh").contains("total"));
    }

    @Test
    public void checkDirectory() {
        ShellClient client = new ShellClient(new ProcessBuilder().directory(new File("src/test/resources")));
        assertTrue(client.execute("bash", "-c", "pwd").trim().endsWith("/src/test/resources"));
    }
}
