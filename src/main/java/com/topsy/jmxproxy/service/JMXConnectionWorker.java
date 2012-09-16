package com.topsy.jmxproxy.service;

import com.topsy.jmxproxy.domain.MBean;
import com.topsy.jmxproxy.domain.Host;

import java.io.IOException;

//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;

import javax.management.MalformedObjectNameException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;

public class JMXConnectionWorker {
    private static final Logger LOG = Logger.getLogger(JMXConnectionWorker.class);

    private Host host;
    private String hostName;

    private JMXServiceURL url;
    private JMXConnector connection;
    private MBeanServerConnection server;
    private Long connectTime = Long.MAX_VALUE;

    public JMXConnectionWorker(String hostName) throws Exception {
        url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + hostName + "/jmxrmi");
        this.connection = null;
        this.hostName = hostName;
    }

    public Host getHost() {
        connect();
        return host;
    }

    private void connect() {
        if (connection != null && System.currentTimeMillis() - connectTime < 1000 * 60) {
            LOG.debug("using cached connection to " + url);
            return;
        } else if (connection != null) {
            LOG.info("reconnecting to " + url);
            disconnect();
        }

        host = new Host();
        host.setHostName(hostName);

        try {
            connection = JMXConnectorFactory.connect(url, null);
            server = connection.getMBeanServerConnection();
            LOG.debug("connected to mbean server " + url);

            for (String domainName : server.getDomains()) {
                LOG.debug("discovered domain " + domainName);

                for (ObjectName mbeanName : server.queryNames(new ObjectName(domainName + ":*"), null)) {
                    LOG.debug("discovered mbean " + mbeanName);

                    MBean mbean = host.addMBean(mbeanName.toString());
//                    mbeans.put(mbean, new MBean(server.getMBeanInfo(mbean).getAttributes()));
                }
            }

            connectTime = System.currentTimeMillis();
        } catch (Exception e) {
            LOG.error("failed to connect to " + url, e);
        } finally {
            if (connection != null) {
                disconnect();
            }
        }
    }

    public synchronized void disconnect() {
        try {
            connection.close();
            connection = null;
            connectTime = Long.MAX_VALUE;

            LOG.debug("disconnected from " + url);
        } catch (IOException e) {
            LOG.error("failed to disconnect from " + url, e);
        }
    }
}
/*
public class JMXConnectionWorker {
    private static final Logger logger = Logger.getLogger(JMXConnectionWorker.class);

    private JMXServiceURL url;
    private JMXConnector connection = null;
    private MBeanServerConnection server;
    private Long connectTime = Long.MAX_VALUE;

    private String[] domains;
    private Map<ObjectName, MBean> mbeans;

    public JMXConnectionWorker(String host) throws Exception {
        url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + "/jmxrmi");
        mbeans = new HashMap<ObjectName, MBean>();
    }

    private void connect() {
        if (connection != null && System.currentTimeMillis() - connectTime < 1000 * 60) {
            logger.debug("using cached connection to " + url);
            return;
        } else if (connection != null) {
            logger.info("reconnecting to " + url);
            disconnect();
        }

        try {
            connection = JMXConnectorFactory.connect(url, null);
            server = connection.getMBeanServerConnection();
            logger.debug("connected to mbean server " + url);

            domains = server.getDomains();
            for (String domain : domains) {
                logger.debug("discovered domain " + domain);
                for (ObjectName mbean : server.queryNames(new ObjectName(domain + ":*"), null)) {
                    logger.debug("discovered mbean " + mbean);
                    mbeans.put(mbean, new MBean(server.getMBeanInfo(mbean).getAttributes()));
                }
            }

            connectTime = System.currentTimeMillis();
        } catch (Exception e) {
            logger.error("failed to connect to " + url, e);
            disconnect();
        }
    }

    public synchronized void disconnect() {
        try {
            mbeans.clear();

            connection.close();
            connection = null;
            connectTime = Long.MAX_VALUE;

            logger.debug("disconnected from " + url);
        } catch (IOException e) {
            logger.error("failed to disconnect from " + url, e);
        }
    }

    public String[] getDomains() {
        connect();
        return domains;
    }

    public String[] getMBeans() {
        return getMBeans("");
    }

    public String[] getMBeans(String domain) {
        connect();

        List mbeans = new ArrayList();
        for (ObjectName mbean : this.mbeans.keySet()) {
            String name = mbean.toString();
            if (name.startsWith(domain)) {
                mbeans.add(name);
            }
        }
        return (String[])mbeans.toArray(new String[0]);
    }

    public String[] getAttributes(String mbean) {
        connect();

        try {
            return mbeans.get(new ObjectName(mbean)).getAttributes();
        } catch (MalformedObjectNameException e) {
            return new String[0];
        }
    }

    public Object getAttributeValue(String mbean, String attributeKey) {
        connect();

        try {
            ObjectName mbeanKey = new ObjectName(mbean);

            if (! mbeans.get(mbeanKey).hasAttribute(attributeKey)) {
                return null;
            }
            Attribute attribute = mbeans.get(mbeanKey).getAttribute(attributeKey);
            attribute.setValue(server.getAttribute(mbeanKey, attributeKey));

            return attribute.getValue();
        } catch (Exception e) {
            logger.error("mbean error fetching attribute " + attributeKey + " for " + mbean + " on " + url, e);
            return null;
        }
    }
}
*/
