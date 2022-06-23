package io.jtest.utils.clients.jmx;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.management.MalformedObjectNameException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class JmxClientTest {

    @Test
    @Disabled
    public void testJmxProxy() throws MalformedObjectNameException {
        JmxClient jmxClient = new JmxClient("service:jmx:rmi:///jndi/rmi://domnexfrontmwqa01.mw.server.lan:11193/server",
                "control", "shu8cooS");
        PollerMBean proxy = jmxClient.getMBeanProxy("MigraeneDomain:name=MigraeneDomainPollerMBean", PollerMBean.class);
        proxy.switchPollerActive();
    }

    @Test
    public void testInvalidJmxConnection() {
        Exception e = assertThrows(RuntimeException.class, () -> new JmxClient("service:jmx:rmi:///jndi/rmi://invalid.test.de:11193/server", "control", "shu8cooS"));
        assertTrue(e.getMessage().contains("Failed to retrieve"));
    }
}
