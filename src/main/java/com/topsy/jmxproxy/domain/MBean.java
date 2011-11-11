package com.topsy.jmxproxy.domain;

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanAttributeInfo;

import com.topsy.jmxproxy.domain.Attribute;

public class MBean {
    private Map<String, Attribute> attributes;

    public MBean(MBeanAttributeInfo[] attributes) {
        this.attributes = new HashMap<String, Attribute>();

        for (MBeanAttributeInfo attribute : attributes) {
            if (attribute.isReadable()) {
                this.attributes.put(attribute.getName(), new Attribute());
            }
        }
    }

    public String[] getAttributes() {
        return (String[])attributes.keySet().toArray(new String[0]);
    }

    public boolean hasAttribute(String attribute) {
        return attributes.containsKey(attribute);
    }

    public Attribute getAttribute(String attribute) {
        return attributes.get(attribute);
    }
}


