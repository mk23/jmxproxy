package com.topsy.jmxproxy.service;

import com.topsy.jmxproxy.domain.Host;
import com.topsy.jmxproxy.service.JMXConnectionWorker;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

// import org.springframework.jmx.export.annotation.ManagedResource;
// import org.springframework.jmx.export.annotation.ManagedOperation;
// import org.springframework.jmx.export.annotation.ManagedOperationParameters;
// import org.springframework.jmx.export.annotation.ManagedOperationParameter;
// import org.springframework.jmx.export.annotation.ManagedAttribute;

// import org.springframework.scheduling.annotation.Async;
// import org.springframework.scheduling.annotation.Scheduled;

public class JMXConnectionManager {
    private static final Logger LOG = Logger.getLogger(JMXConnectionManager.class);

    private Map<String, JMXConnectionWorker> hosts;

    public JMXConnectionManager() {
        hosts = new HashMap<String, JMXConnectionWorker>();
    }

    public synchronized Host getHost(String host) throws Exception {
        if (!hosts.containsKey(host)) {
            hosts.put(host, new JMXConnectionWorker(host));
        }

        return hosts.get(host).getHost();
    }
}
/*
@ManagedResource(objectName="jmxproxy:service=JMXConnectionManager")
public class JMXConnectionManager {
    private static final Logger logger = Logger.getLogger(JMXConnectionManager.class);

    private Map<String, JMXConnectionWorker> hosts;

    public JMXConnectionManager() {
        hosts = new HashMap<String, JMXConnectionWorker>();
    }

    @ManagedAttribute(description="List of connected hosts")
    public String[] getHosts() {
        return (String[])hosts.keySet().toArray(new String[0]);
    }

    @ManagedOperation(description="Remove a specified host")
    @ManagedOperationParameters({
        @ManagedOperationParameter(name="host", description="host[:port]")
    })
    public void removeHost(String host) {
        if (hosts.containsKey(host)) {
            hosts.get(host).disconnect();
            synchronized(hosts) {
                hosts.remove(host);
            }
        }
    }

    @ManagedOperation(description="Get domains for specified host")
    @ManagedOperationParameters({
        @ManagedOperationParameter(name="host", description="host[:port]")
    })
    public String[] getDomains(String host) throws Exception {
        return getJMXConnection(host).getDomains();
    }

    @ManagedOperation(description="Get mbeans for specified host and domain")
    @ManagedOperationParameters({
        @ManagedOperationParameter(name="host", description="host[:port]"),
        @ManagedOperationParameter(name="domain", description="mbean domain")
    })
    public String[] getMBeans(String host, String domain) throws Exception {
        return getJMXConnection(host).getMBeans(domain);
    }

    public String[] getAttributes(String host, String mbean) throws Exception {
        return getJMXConnection(host).getAttributes(mbean);
    }

    public Object getAttributeValue(String host, String mbean, String attribute) throws Exception {
        return getJMXConnection(host).getAttributeValue(mbean, attribute);
    }

    private JMXConnectionWorker getJMXConnection(String host) throws Exception {
        if (!hosts.containsKey(host)) {
            hosts.put(host, new JMXConnectionWorker(host));
        }

        return hosts.get(host);
    }
}
*/
