package com.topsy.jmxproxy.jmx;

import com.topsy.jmxproxy.core.Attribute;
import com.topsy.jmxproxy.core.Host;
import com.topsy.jmxproxy.core.MBean;

import java.io.IOException;

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

    private Host host;
    private JMXServiceURL url;
    private int cacheDuration;
    private long cacheTime;
    private long accessTime;

    public ConnectionWorker(String hostName) throws Exception {
        ConnectionWorker(hostName, 1000 * 60 * 5);
    }

    public ConnectionWorker(String hostName, int cacheDuration) throws Exception {
        this.cacheDuration = cacheDuration;

        url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + hostName + "/jmxrmi");
    }

    public synchronized Host getHost() {
        if (host == null || System.currentTimeMillis() - cacheTime > cacheDuration) {
            LOG.debug("fetching new values for " + url + ": " + (System.currentTimeMillis() - cacheTime));
            fetchJMXValues();
        }

        accessTime = System.currentTimeMillis();
        return host;
    }

    public boolean isExpired() {
        return isExpired(1000 * 60 * 5);
    }

    public boolean isExpired(int accessDuration) {
        return System.currentTimeMillis() - accessTime > accessDuration
    }

    private void fetchJMXValues() {
        JMXConnector connection = null;
        MBeanServerConnection server = null;

        try {
            connection = JMXConnectorFactory.connect(url, null);
            server = connection.getMBeanServerConnection();
            LOG.debug("connected to mbean server " + url);

            host = new Host();
            for (String domainName : server.getDomains()) {
                LOG.debug("discovered domain " + domainName);

                for (ObjectName mbeanName : server.queryNames(new ObjectName(domainName + ":*"), null)) {
                    LOG.debug("discovered mbean " + mbeanName);

                    MBean mbean = host.addMBean(domainName, mbeanName.toString());
                    for (MBeanAttributeInfo attributeObject : server.getMBeanInfo(mbeanName).getAttributes()) {
                        if (attributeObject.isReadable()) {
                            try {
                                Attribute attribute = mbean.addAttribute(attributeObject.getName());
                                attribute.setAttributeValue(server.getAttribute(mbeanName, attributeObject.getName()));
                            } catch (java.rmi.UnmarshalException e) {
                                LOG.error("failed to add attribute " + attributeObject.toString() + ": " + e);
                            } catch (javax.management.AttributeNotFoundException e) {
                                LOG.error("failed to add attribute " + attributeObject.toString() + ": " + e);
                            } catch (javax.management.RuntimeMBeanException e) {
                                LOG.error("failed to add attribute " + attributeObject.toString() + ": " + e);
                            } catch (java.lang.NullPointerException e) {
                                LOG.error("failed to add attribute " + attributeObject.toString() + ": " + e);
                            }
                        }
                    }
                }
            }

            cacheTime = System.currentTimeMillis();
        } catch (Exception e) {
            LOG.error("failed to connect to " + url, e);
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
