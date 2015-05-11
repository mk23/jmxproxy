package com.github.mk23.jmxproxy.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class History {
    private static final Logger LOG = LoggerFactory.getLogger(History.class);

    private Attribute[] attributes;
    private int current;

    public History(int size) {
        attributes = new Attribute[size];
    }

    public void addAttributeValue(Object attributeValue) {
        Attribute attribute = new Attribute(attributeValue);
        attributes[++current % attributes.length] = attribute;
        if (current == Integer.MAX_VALUE) {
            current = current % attributes.length + attributes.length;
        }
    }

    public Attribute getAttribute() {
        return attributes[current % attributes.length];
    }

    public Attribute[] getAttributes() {
        return getAttributes(attributes.length);
    }

    public Attribute[] getAttributes(int limit) {
        int size = Math.min(limit != 0 ? limit : Integer.MAX_VALUE, Math.min(current, attributes.length));
        Attribute[] rval = new Attribute[size];

        for (int i = 0; i < size; i++) {
            rval[i] = attributes[(current - i) % attributes.length];
        }

        return rval;
    }
}
