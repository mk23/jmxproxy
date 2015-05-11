package com.topsy.jmxproxy.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MBean implements JsonSerializable {
    private Map<String, History> attributes;
    private ThreadLocal<Integer> limit = new ThreadLocal<Integer>() {
        @Override protected Integer initialValue() {
            return -1;
        }
    };

    public MBean() {
        attributes = new HashMap<String, History>();
    }

    public History addHistory(String attributeName, int size) {
        if (!attributes.containsKey(attributeName)) {
            History history = new History(size);
            attributes.put(attributeName, history);
        }

        return attributes.get(attributeName);
    }

    public MBean setLimit(Integer limit) {
        this.limit.set(limit);
        return this;
    }

    public Set<String> getAttributes() {
        return attributes.keySet();
    }

    public Attribute getAttribute(String attribute) {
        History history = attributes.get(attribute);
        if (history == null) {
            return null;
        }

        return history.getAttribute();
    }

    public Attribute[] getAttributes(String attribute, int limit) {
        History history = attributes.get(attribute);
        if (history == null) {
            return new Attribute[0];
        }

        return history.getAttributes(limit);
    }

    public void serialize(JsonGenerator jgen, SerializerProvider sp) throws IOException, JsonProcessingException {
        buildJson(jgen);
    }

    public void serializeWithType(JsonGenerator jgen, SerializerProvider sp, TypeSerializer ts) throws IOException, JsonProcessingException {
        buildJson(jgen);
    }

    private void buildJson(JsonGenerator jgen) throws IOException, JsonProcessingException {
        int limit = this.limit.get();

        jgen.writeStartObject();
        for (Map.Entry<String, History>attributeEntry : attributes.entrySet()) {
            if (limit < 0) {
                jgen.writeObjectField(attributeEntry.getKey(), attributeEntry.getValue().getAttribute());
            } else {
                jgen.writeObjectField(attributeEntry.getKey(), attributeEntry.getValue().getAttributes(limit));
            }
        }
        jgen.writeEndObject();
    }
}
