package com.topsy.jmxproxy.jmx;

import com.topsy.jmxproxy.core.Attribute;
import com.topsy.jmxproxy.core.Host;
import com.topsy.jmxproxy.core.MBean;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionWorker {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionWorker.class);

    private Host host = null;
    private ConnectionCredentials auth = null;

    private JMXServiceURL url;

    private long accessTime;
    private long cacheTime;

    private ScheduledExecutorService fetch;

    public ConnectionWorker(String hostName, ConnectionCredentials auth, long cacheDuration) throws Exception {
        this.auth = auth;

        url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + hostName + "/jmxrmi");
        fetch = Executors.newSingleThreadScheduledExecutor();

        fetch.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                fetchJMXValues();
            }
        }, cacheDuration, cacheDuration, TimeUnit.MINUTES);

        fetchJMXValues();
    }

    public Host getHost() throws SecurityException {
        accessTime = System.currentTimeMillis();
        return host;
    }

    public boolean checkCredentials(ConnectionCredentials auth) {
        return auth == this.auth || auth != null && auth.equals(this.auth) || this.auth != null && this.auth.equals(auth);
    }

    public boolean isExpired(long accessDuration) {
        return System.currentTimeMillis() - accessTime > accessDuration * 60 * 1000;
    }

    public void shutdown() {
        if (!fetch.isShutdown()) {
            fetch.shutdown();
        }
    }

    private synchronized void fetchJMXValues() throws SecurityException {
        JMXConnector connection = null;
        MBeanServerConnection server = null;
        Map<String, Object> environment = null;

        if (this.auth != null) {
            environment = new HashMap<String, Object>();
            environment.put(JMXConnector.CREDENTIALS, new String[]{auth.getUsername(), auth.getPassword()});
        }

        try {
            LOG.debug("connecting to mbean server " + url);

            connection = JMXConnectorFactory.connect(url, environment);
            server = connection.getMBeanServerConnection();

            host = new Host();
            for (String domainName : server.getDomains()) {
                LOG.debug("discovered domain " + domainName);

                try {
                    for (ObjectName mbeanName : server.queryNames(new ObjectName(domainName + ":*"), null)) {
                        LOG.debug("discovered mbean " + mbeanName);

                        MBean mbean = host.addMBean(domainName, mbeanName.toString());
                        try {
                            for (MBeanAttributeInfo attributeObject : server.getMBeanInfo(mbeanName).getAttributes()) {
                                if (attributeObject.isReadable()) {
                                    try {
                                        Attribute attribute = mbean.addAttribute(attributeObject.getName());
                                        attribute.setAttributeValue(server.getAttribute(mbeanName, attributeObject.getName()));
                                    } catch (java.lang.NullPointerException e) {
                                        LOG.error("failed to add attribute " + attributeObject.toString() + ": " + e);
                                    } catch (java.rmi.UnmarshalException e) {
                                        LOG.error("failed to add attribute " + attributeObject.toString() + ": " + e);
                                    } catch (javax.management.AttributeNotFoundException e) {
                                        LOG.error("failed to add attribute " + attributeObject.toString() + ": " + e);
                                    } catch (javax.management.MBeanException e) {
                                        LOG.error("failed to add attribute " + attributeObject.toString() + ": " + e);
                                    } catch (javax.management.RuntimeMBeanException e) {
                                        LOG.error("failed to add attribute " + attributeObject.toString() + ": " + e);
                                    }
                                }
                            }
                        } catch (javax.management.InstanceNotFoundException e) {
                            LOG.error("failed to get mbean info for " + mbeanName, e);
                        } catch (javax.management.IntrospectionException e) {
                            LOG.error("failed to get mbean info for " + mbeanName, e);
                        } catch (javax.management.ReflectionException e) {
                            LOG.error("failed to get mbean info for " + mbeanName, e);
                        }
                    }
                } catch (javax.management.MalformedObjectNameException e) {
                    LOG.error("invalid object name: " + domainName + ":*", e);
                }
            }

            cacheTime = System.currentTimeMillis();
        } catch (IOException e) {
            host = null;
            LOG.error("communication failure with " + url, e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    LOG.debug("disconnected from " + url);
                } catch (IOException e) {
                    LOG.error("failed to disconnect from " + url, e);
                } finally {
                    connection = null;
                }
            }
        }
    }
}
