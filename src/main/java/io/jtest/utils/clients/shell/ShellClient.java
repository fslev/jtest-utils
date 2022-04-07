package io.jtest.utils.clients.shell;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ShellClient {

    private static final Logger LOG = LogManager.getLogger();

    private final ProcessBuilder processBuilder;

    public ShellClient() {
        this.processBuilder = new ProcessBuilder();
    }

    public ShellClient(ProcessBuilder processBuilder) {
        this.processBuilder = processBuilder;
    }

    public String execute(String... command) {
        StringBuilder outputBuffer = new StringBuilder();
        try {
            Process p = startProcess(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8));
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

    public Process startProcess(String... command) {
        LOG.info("Execute command \"{}\"", Arrays.toString(command));
        this.processBuilder.command(command);
        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
