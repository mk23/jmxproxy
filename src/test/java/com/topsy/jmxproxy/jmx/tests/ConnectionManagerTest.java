package com.topsy.jmxproxy.jmx.tests;

import com.topsy.jmxproxy.JMXProxyConfiguration;
import com.topsy.jmxproxy.jmx.ConnectionManager;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ConnectionManagerTest {
    private final String validHost        = "localhost:" + System.getProperty("com.sun.management.jmxremote.port");
    private final String invalidHost      = "localhost:0";

    private final String validMBean       = "java.lang:type=OperatingSystem";
    private final String invalidMBean     = "java.lang:type=InvalidMBean";

    private final String validAttribute   = "Name";
    private final String invalidAttribute = "InvalidAttribute";

    /* Host tests */
    @Test
    public void checkValidHost() throws Exception {
        final ConnectionManager manager = new ConnectionManager(new JMXProxyConfiguration().getJMXProxyServiceConfiguration());

        assertNotNull(manager.getHost(validHost, null));
    }

    @Test
    public void checkInvalidHost() throws Exception {
        final ConnectionManager manager = new ConnectionManager(new JMXProxyConfiguration().getJMXProxyServiceConfiguration());

        assertNull(manager.getHost(invalidHost, null));
    }

    /* MBean tests */
    @Test
    public void checkValidHostMBeans() throws Exception {
        final ConnectionManager manager = new ConnectionManager(new JMXProxyConfiguration().getJMXProxyServiceConfiguration());

        assertTrue(manager.getHost(validHost, null).getMBeans().contains(validMBean));
    }

    @Test
    public void checkValidHostValidMBean() throws Exception {
        final ConnectionManager manager = new ConnectionManager(new JMXProxyConfiguration().getJMXProxyServiceConfiguration());

        assertNotNull(manager.getHost(validHost, null).getMBean(validMBean));
    }

    @Test
    public void checkValidHostInvalidMBean() throws Exception {
        final ConnectionManager manager = new ConnectionManager(new JMXProxyConfiguration().getJMXProxyServiceConfiguration());

        assertNull(manager.getHost(validHost, null).getMBean(invalidMBean));
    }

    /* Attribute tests */
    @Test
    public void checkValidHostValidMBeanAttributes() throws Exception {
        final ConnectionManager manager = new ConnectionManager(new JMXProxyConfiguration().getJMXProxyServiceConfiguration());

        assertTrue(manager.getHost(validHost, null).getMBean(validMBean).getAttributes().contains(validAttribute));
    }

    @Test
    public void checkValidHostValidMBeanValidAttribute() throws Exception {
        final ConnectionManager manager = new ConnectionManager(new JMXProxyConfiguration().getJMXProxyServiceConfiguration());

        assertNotNull(manager.getHost(validHost, null).getMBean(validMBean).getAttribute(validAttribute));
    }

    @Test
    public void checkValidHostValidMBeanInvalidAttribute() throws Exception {
        final ConnectionManager manager = new ConnectionManager(new JMXProxyConfiguration().getJMXProxyServiceConfiguration());

        assertNull(manager.getHost(validHost, null).getMBean(validMBean).getAttribute(invalidAttribute));
    }
}
