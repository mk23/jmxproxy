package com.topsy.jmxproxy.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

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

    public void serialize(JsonGenerator jgen, SerializerProvider sp) throws IOException, JsonProcessingException {
        buildJson(jgen);
    }

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
