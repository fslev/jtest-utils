package io.jtest.utils.clients.jsch;

import com.jcraft.jsch.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JschClient {
    private static final Logger LOG = LogManager.getLogger();
    private static final int TIMEOUT_SECONDS = 60000;
    private final String host;
    private final int port;
    private final String user;
    private final String privateKey;
    private final Session session;

    public JschClient(String host, int port, String user, String pwd, String privateKey, Properties config) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.privateKey = privateKey;
        JSch jsch = new JSch();
        try {
            jsch.addIdentity(privateKey);
            this.session = jsch.getSession(user, host, port);
            session.setPassword(pwd);
            if (config != null) {
                session.setConfig(config);
            }
            session.setTimeout(TIMEOUT_SECONDS);
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }
    }

    public void connect() {
        try {
            LOG.info("Connecting over SSH to \"{}:{}\" with user \"{}\" and privateKey \"{}\"", host, port, user, privateKey);
            this.session.connect();
            LOG.info("Connected");
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }
    }

    public String sendCommand(String cmd) {
        try {
            LOG.info("Execute command over SSH: \"{}\"", cmd);
            Channel channel = this.session.openChannel("exec");
            ((ChannelExec) channel).setCommand(cmd);
            InputStream commandOutput = channel.getInputStream();
            InputStream commandErrOutput = ((ChannelExec) channel).getErrStream();
            channel.connect();
            StringBuilder outputBuffer = new StringBuilder();
            int readByte = commandOutput.read();
            while (readByte != 0xffffffff) {
                outputBuffer.append((char) readByte);
                readByte = commandOutput.read();
            }
            readByte = commandErrOutput.read();
            while (readByte != 0xffffffff) {
                outputBuffer.append((char) readByte);
                readByte = commandErrOutput.read();
            }
            channel.disconnect();
            LOG.debug("Output over SSH: {}", outputBuffer.toString());
            return outputBuffer.toString();
        } catch (JSchException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect() {
        this.session.disconnect();
        LOG.debug("SSH connection closed");
    }
}
