package com.topsy.jmxproxy.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;

import java.lang.reflect.Array;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

public class Attribute implements JsonSerializable {
    private Object attributeValue;

    public void setAttributeValue(Object attributeValue) {
        this.attributeValue = attributeValue;
    }

    public void serialize(JsonGenerator jgen, SerializerProvider sp) throws IOException, JsonProcessingException {
        buildJson(jgen, attributeValue);
    }

    public void serializeWithType(JsonGenerator jgen, SerializerProvider sp, TypeSerializer ts) throws IOException, JsonProcessingException {
        buildJson(jgen, attributeValue);
    }

    private void buildJson(JsonGenerator jgen, Object objectValue) throws IOException, JsonProcessingException {
        if (objectValue == null) {
            jgen.writeNull();
        } else if (objectValue.getClass().isArray()) {
            jgen.writeStartArray();
            int length = Array.getLength(objectValue);
            for (int i = 0; i < length; i++) {
                buildJson(jgen, Array.get(objectValue, i));
            }
            jgen.writeEndArray();
        } else if (objectValue instanceof TabularData) {
            TabularData data = (TabularData) objectValue;
            jgen.writeStartArray();
            for (Object objectEntry : data.values()) {
                buildJson(jgen, objectEntry);
            }
            jgen.writeEndArray();
        } else if (objectValue instanceof CompositeData) {
            CompositeData data = (CompositeData) objectValue;
            jgen.writeStartObject();
            for (String objectEntry : data.getCompositeType().keySet()) {
                jgen.writeFieldName(objectEntry);
                buildJson(jgen, data.get(objectEntry));
            }
            jgen.writeEndObject();
        } else if (objectValue instanceof Number) {
            jgen.writeNumber(((Number)objectValue).toString());
        } else if (objectValue instanceof Boolean) {
            jgen.writeBoolean((Boolean)objectValue);
        } else {
            jgen.writeString(objectValue.toString());
        }
    }
}
