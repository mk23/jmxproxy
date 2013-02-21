package com.topsy.jmxproxy.core;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(using=AttributeSerializer.class)
public class Attribute {
    private Object attributeValue;

    public void setAttributeValue(Object attributeValue) {
        this.attributeValue = attributeValue;
    }

    public Object getAttributeValue() {
        return attributeValue;
    }
}
