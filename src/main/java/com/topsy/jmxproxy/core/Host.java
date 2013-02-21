package com.topsy.jmxproxy.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(using=HostSerializer.class)
public class Host {
    private Map<String, MBean> mbeans;
    private Map<String, List<String>> domains;

    public Host() {
        this.mbeans = new HashMap<String, MBean>();
        this.domains = new HashMap<String, List<String>>();
    }

    public MBean addMBean(String domain, String mbeanName) {
        if (!domains.containsKey(domain)) {
            domains.put(domain, new ArrayList<String>());
        }
        domains.get(domain).add(mbeanName);

        MBean mbean = new MBean();
        mbeans.put(mbeanName, mbean);

        return mbean;
    }

    public Map<String, MBean> getMBeans() {
        return mbeans;
    }

    public Map<String, List<String>> getDomains() {
        return domains;
    }
}
