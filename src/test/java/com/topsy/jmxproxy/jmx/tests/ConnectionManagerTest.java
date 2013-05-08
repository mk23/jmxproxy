package com.topsy.jmxproxy.jmx.tests;

import com.topsy.jmxproxy.JMXProxyConfiguration;
import com.topsy.jmxproxy.jmx.ConnectionManager;

import javax.ws.rs.core.Response;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ConnectionManagerTest {
    private final String validHost        = "localhost:" + System.getProperty("com.sun.management.jmxremote.port");
    private final String invalidHost      = "localhost:0";

    private final String validDomain      = "java.lang";
    private final String invalidDomain    = "invalid.domain";

    private final String validMBean       = "java.lang:type=OperatingSystem";
    private final String invalidMBean     = "java.lang:type=InvalidDomain";

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

    /* Domain tests */
    @Test
    public void checkValidHostDomains() throws Exception {
        final ConnectionManager manager = new ConnectionManager(new JMXProxyConfiguration().getJMXProxyServiceConfiguration());

        assertTrue(manager.getHost(validHost, null).getDomains().contains(validDomain));
    }

    @Test
    public void checkValidHostValidDomain() throws Exception {
        final ConnectionManager manager = new ConnectionManager(new JMXProxyConfiguration().getJMXProxyServiceConfiguration());

        assertNotNull(manager.getHost(validHost, null).getDomain(validDomain));
    }

    @Test
    public void checkValidHostInvalidDomain() throws Exception {
        final ConnectionManager manager = new ConnectionManager(new JMXProxyConfiguration().getJMXProxyServiceConfiguration());

        assertNull(manager.getHost(validHost, null).getDomain(invalidDomain));
    }

    /* MBean tests */
    @Test
    public void checkValidHostValidDomainMBeans() throws Exception {
        final ConnectionManager manager = new ConnectionManager(new JMXProxyConfiguration().getJMXProxyServiceConfiguration());

        assertTrue(manager.getHost(validHost, null).getDomain(validDomain).getMBeans().contains(validMBean));
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
