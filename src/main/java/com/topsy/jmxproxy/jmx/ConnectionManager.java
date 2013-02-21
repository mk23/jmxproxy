package com.topsy.jmxproxy.service;

import com.topsy.jmxproxy.domain.Host;
import com.topsy.jmxproxy.service.JMXConnectionWorker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.apache.log4j.Logger;

import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.annotation.ManagedAttribute;

@ManagedResource(objectName="jmxproxy:service=JMXConnectionManager")
public class JMXConnectionManager {
    private static final Logger LOG = Logger.getLogger(JMXConnectionManager.class);

    private Map<String, JMXConnectionWorker> hosts;

    @ManagedAttribute(description="List of cached hosts")
    public List<String> getHosts() {
        return new ArrayList<String>(hosts.keySet());
    }

    public JMXConnectionManager() {
        hosts = new HashMap<String, JMXConnectionWorker>();
    }

    public synchronized Host getHost(String host) throws Exception {
        if (!hosts.containsKey(host)) {
            LOG.debug("creating new jmx connection worker");
            hosts.put(host, new JMXConnectionWorker(host));
        }

        return hosts.get(host).getHost();
    }
}
