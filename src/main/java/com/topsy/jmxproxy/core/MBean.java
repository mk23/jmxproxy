package com.topsy.jmxproxy.core;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializableWithType;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;

public class MBean implements JsonSerializableWithType {
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
    public void serialize(JsonGenerator jgen, SerializerProvider sp) throws IOException, JsonProcessingException {
        buildJson(jgen);
    }

    @Override
    public void serializeWithType(JsonGenerator jgen, SerializerProvider sp, TypeSerializer ts) throws IOException, JsonProcessingException {
        buildJson(jgen);
    }

    private void buildJson(JsonGenerator jgen) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        for (Map.Entry<String, Attribute>attributeEntry : attributes.entrySet()) {
            jgen.writeObjectField(attributeEntry.getKey(), attributeEntry.getValue());
        }
        jgen.writeEndObject();
    }
}
