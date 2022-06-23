package io.jtest.utils.clients.shell;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShellClientTest {

    @Test
    @EnabledOnOs(OS.LINUX)
    public void testSimpleShellCommand() {
        ShellClient client = new ShellClient();
        assertTrue(client.execute("bash", "-c", "ls -alh").contains("total"));
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    public void checkDirectory() {
        ShellClient client = new ShellClient(new ProcessBuilder().directory(new File("src/test/resources")));
        assertTrue(client.execute("bash", "-c", "pwd").trim().endsWith("/src/test/resources"));
    }
}
