package io.jtest.utils.clients.jmx;

public interface PollerMBean {

    void switchPollerActive();

    void switchPollerInactive();

    boolean isPollerActive();
}
