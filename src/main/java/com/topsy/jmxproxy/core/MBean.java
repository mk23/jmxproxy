package com.topsy.jmxproxy.core;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(using=MBeanSerializer.class)
public class MBean {
    private Map<String, Attribute> attributes;

    public MBean() {
        this.attributes = new HashMap<String, Attribute>();
    }

    public Attribute addAttribute(String attributeName) {
        Attribute attribute = new Attribute();
        attributes.put(attributeName, attribute);

        return attribute;
    }

    public Map<String, Attribute> getAttributes() {
        return attributes;
    }
}
