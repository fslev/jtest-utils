package io.jtest.utils.clients.shell;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ShellClient {

    private static final Logger LOG = LogManager.getLogger();

    private final ProcessBuilder processBuilder;

    public ShellClient() {
        this.processBuilder = new ProcessBuilder();
    }

    public String command(String... command) {
        LOG.info("Executing shell command \"{}\"", Arrays.toString(command));
        this.processBuilder.command(command);
        StringBuilder outputBuffer = new StringBuilder();
        try {
            Process p = processBuilder.start();
            InputStream stdInput = p.getInputStream();
            InputStream stdError = p.getErrorStream();

            int readByte = stdInput.read();
            while (readByte != 0xffffffff) {
                outputBuffer.append((char) readByte);
                readByte = stdInput.read();
            }
            readByte = stdError.read();
            while (readByte != 0xffffffff) {
                outputBuffer.append((char) readByte);
                readByte = stdError.read();
            }
            p.waitFor();
            p.destroy();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        LOG.debug("Shell output: {}", outputBuffer.toString());
        return outputBuffer.toString();
    }
}
