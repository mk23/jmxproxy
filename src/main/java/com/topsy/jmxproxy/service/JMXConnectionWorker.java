package com.topsy.jmxproxy.service;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MalformedObjectNameException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import com.topsy.jmxproxy.domain.Attribute;
import com.topsy.jmxproxy.domain.MBean;

public class JMXConnectionWorker {
    private static final Logger logger = Logger.getLogger(JMXConnectionWorker.class);

    private JMXServiceURL url;
    private JMXConnector connection;
    private MBeanServerConnection server;

    private String[] domains;
    private Map<ObjectName, MBean> mbeans;

    public JMXConnectionWorker(String host) throws Exception {
        url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + "/jmxrmi");
        mbeans = new HashMap<ObjectName, MBean>();

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

        fetchAttributeValues();
    }

    public void disconnect() {
        try {
            connection.close();
            logger.debug("disconnected from " + url);
        } catch (IOException e) {
            logger.error("failed to disconnect from " + url);
        }
    }

    public String[] getDomains() {
        return domains;
    }

    public String[] getMBeans() {
        return getMBeans("");
    }

    public String[] getMBeans(String domain) {
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
        try {
            return mbeans.get(new ObjectName(mbean)).getAttributes();
        } catch (MalformedObjectNameException e) {
            return new String[0];
        }
    }

    public void setAttributeValue(ObjectName mbeanKey, String attributeKey) {
        Attribute attribute = mbeans.get(mbeanKey).getAttribute(attributeKey);
        try {
            attribute.setValue(server.getAttribute(mbeanKey, attributeKey));
            attribute.setMonitored(true);
        } catch (Exception e) {
            logger.error("mbean error fetching attribute " + attributeKey + " for " + mbeanKey + " on " + url);
            logger.debug(e.fillInStackTrace());
            attribute.setMonitored(false);
            attribute.setValue(null);
        }
    }

    public Object getAttributeValue(String mbean, String attributeKey) {
        try {
            return getAttributeValue(new ObjectName(mbean), attributeKey);
        } catch (MalformedObjectNameException e) {
            return null;
        }
    }

    public Object getAttributeValue(ObjectName mbeanKey, String attributeKey) {
        if (! mbeans.get(mbeanKey).hasAttribute(attributeKey)) {
            return null;
        }

        Attribute attribute = mbeans.get(mbeanKey).getAttribute(attributeKey);
        if (! attribute.isMonitored()) {
            logger.debug("adding new attribute monitor (" + mbeanKey + ") " + attributeKey);
            setAttributeValue(mbeanKey, attributeKey);
        }

        return attribute.getValue();
    }

    public void fetchAttributeValues() {
        logger.debug("fetching attribute values from " + url);
        for (ObjectName mbeanKey : mbeans.keySet()) {
            MBean mbean = mbeans.get(mbeanKey);
            for (String attributeKey : mbean.getAttributes()) {
                Attribute attribute = mbean.getAttribute(attributeKey);
                if (attribute.isMonitored()) {
                    logger.debug("found monitored attribute (" + mbeanKey + ") " + attributeKey);
                    setAttributeValue(mbeanKey, attributeKey);
                }
            }
        }
    }
}
