package com.github.mk23.jmxproxy.jmx.tests;

import com.github.mk23.jmxproxy.JMXProxyConfiguration.JMXProxyApplicationConfiguration;
import com.github.mk23.jmxproxy.jmx.ConnectionManager;

import java.util.Arrays;

import javax.ws.rs.WebApplicationException;

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
        final ConnectionManager manager = new ConnectionManager(new JMXProxyApplicationConfiguration());

        assertNotNull(manager.getHost(validHost));
    }

    @Test
    public void checkInvalidHost() throws Exception {
        final ConnectionManager manager = new ConnectionManager(new JMXProxyApplicationConfiguration());

        assertNull(manager.getHost(invalidHost));
    }

    @Test
    public void checkValidHostWhitelist() throws Exception {
        JMXProxyApplicationConfiguration serviceConfig = new JMXProxyApplicationConfiguration();
        serviceConfig.setAllowedEndpoints(Arrays.asList(new String[] { validHost }));

        final ConnectionManager manager = new ConnectionManager(serviceConfig);

        assertNotNull(manager.getHost(validHost));
    }

    @Test(expected=WebApplicationException.class)
    public void checkInvalidHostWhitelist() throws Exception {
        JMXProxyApplicationConfiguration serviceConfig = new JMXProxyApplicationConfiguration();
        serviceConfig.setAllowedEndpoints(Arrays.asList(new String[] { validHost }));

        final ConnectionManager manager = new ConnectionManager(serviceConfig);

        manager.getHost(invalidHost);
    }

    /* MBean tests */
    @Test
    public void checkValidHostMBeans() throws Exception {
        final ConnectionManager manager = new ConnectionManager(new JMXProxyApplicationConfiguration());

        assertTrue(manager.getHost(validHost).getMBeans().contains(validMBean));
    }

    @Test
    public void checkValidHostValidMBean() throws Exception {
        final ConnectionManager manager = new ConnectionManager(new JMXProxyApplicationConfiguration());

        assertNotNull(manager.getHost(validHost).getMBean(validMBean));
    }

    @Test
    public void checkValidHostInvalidMBean() throws Exception {
        final ConnectionManager manager = new ConnectionManager(new JMXProxyApplicationConfiguration());

        assertNull(manager.getHost(validHost).getMBean(invalidMBean));
    }

    /* Attribute tests */
    @Test
    public void checkValidHostValidMBeanAttributes() throws Exception {
        final ConnectionManager manager = new ConnectionManager(new JMXProxyApplicationConfiguration());

        assertTrue(manager.getHost(validHost).getMBean(validMBean).getAttributes().contains(validAttribute));
    }

    @Test
    public void checkValidHostValidMBeanValidAttribute() throws Exception {
        final ConnectionManager manager = new ConnectionManager(new JMXProxyApplicationConfiguration());

        assertNotNull(manager.getHost(validHost).getMBean(validMBean).getAttribute(validAttribute));
    }

    @Test
    public void checkValidHostValidMBeanInvalidAttribute() throws Exception {
        final ConnectionManager manager = new ConnectionManager(new JMXProxyApplicationConfiguration());

        assertNull(manager.getHost(validHost).getMBean(validMBean).getAttribute(invalidAttribute));
    }
}
