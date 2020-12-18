package io.jtest.utils.clients.jmx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JmxClient {

    private static final Logger LOG = LogManager.getLogger();

    private final JMXConnector jmxConnector;
    private final MBeanServerConnection mbsConnection;

    public JmxClient(String url, String role, String pwd) {
        try {
            if (role != null && pwd != null) {
                Map<String, String[]> env = new HashMap<>();
                env.put(JMXConnector.CREDENTIALS, new String[]{role, pwd});
                this.jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(url), env);
            } else {
                this.jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(url));
            }
            this.mbsConnection = jmxConnector.getMBeanServerConnection();
            LOG.info("Initialised JMX Connection to {}", url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getMBeanProxy(String objectName, Class<T> clazz) throws MalformedObjectNameException {
        return MBeanServerInvocationHandler
                .newProxyInstance(mbsConnection, new ObjectName(objectName), clazz, true);
    }

    public void close() throws IOException {
        this.jmxConnector.close();
    }
}