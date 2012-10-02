package com.topsy.jmxproxy.service;

import com.topsy.jmxproxy.domain.Attribute;
import com.topsy.jmxproxy.domain.Host;
import com.topsy.jmxproxy.domain.MBean;

import java.io.IOException;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;

public class JMXConnectionWorker {
    private static final Logger LOG = Logger.getLogger(JMXConnectionWorker.class);

    private Host host;
    private Long fetchTime;
    private JMXServiceURL url;

    public JMXConnectionWorker(String hostName) throws Exception {
        url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + hostName + "/jmxrmi");
    }

    public Host getHost() {
        if (host == null || System.currentTimeMillis() - fetchTime < 1000 * 60 * 5) {
            LOG.debug("fetching new values for " + url);
            fetchJMXValues();
        }

        return host;
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

            fetchTime = System.currentTimeMillis();
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
