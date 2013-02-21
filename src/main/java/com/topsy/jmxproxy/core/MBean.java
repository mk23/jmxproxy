package com.topsy.jmxproxy.domain;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializable;
import org.codehaus.jackson.map.SerializerProvider;

public class MBean implements JsonSerializable {
    private Map<String, Attribute> attributes;

    public MBean() {
        this.attributes = new HashMap<String, Attribute>();
    }

    public Attribute addAttribute(String attributeName) {
        Attribute attribute = new Attribute();
        attributes.put(attributeName, attribute);

        return attribute;
    }

    @Override
    public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        for (Map.Entry<String, Attribute>attributeEntry : attributes.entrySet()) {
            jgen.writeObjectField(attributeEntry.getKey(), attributeEntry.getValue());
        }
        jgen.writeEndObject();
    }
}
