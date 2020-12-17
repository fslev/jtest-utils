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

    public ProcessBuilder getProcessBuilder() {
        return processBuilder;
    }

    public String execute(String... command) {
        StringBuilder outputBuffer = new StringBuilder();
        try {
            Process p = process(command);
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
        LOG.debug("Process output: {}", outputBuffer.toString());
        return outputBuffer.toString();
    }

    public Process process(String... command) {
        LOG.info("Executing process command \"{}\"", Arrays.toString(command));
        this.processBuilder.command(command);
        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
