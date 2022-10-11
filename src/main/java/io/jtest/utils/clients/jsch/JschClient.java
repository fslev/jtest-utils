package io.jtest.utils.clients.jsch;

import com.jcraft.jsch.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JschClient {
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

    public void connect() throws JSchException {
        this.session.connect();
    }

    public String sendCommand(String cmd) throws IOException, JSchException {
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
        return outputBuffer.toString();
    }

    public void disconnect() {
        this.session.disconnect();
    }
}
