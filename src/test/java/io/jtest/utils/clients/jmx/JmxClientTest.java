package io.jtest.utils.clients.jmx;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.management.MalformedObjectNameException;

@Disabled
public class JmxClientTest {

    @Test
    public void testJmxConnection() throws MalformedObjectNameException {
        JmxClient jmxClient = new JmxClient("service:jmx:rmi:///jndi/rmi://domnexfrontmwqa01.mw.server.lan:11193/server",
                "control", "shu8cooS");
        PollerMBean proxy = jmxClient.getMBeanProxy("MigraeneDomain:name=MigraeneDomainPollerMBean", PollerMBean.class);
        proxy.switchPollerActive();
    }
}
