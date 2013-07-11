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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Attribute implements JsonSerializable {
    private static final Logger LOG = LoggerFactory.getLogger(Attribute.class);

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
        } else if (objectValue instanceof Iterable) {
            Iterable data = (Iterable) objectValue;
            jgen.writeStartArray();
            for (Object objectEntry : data) {
                buildJson(jgen, objectEntry);
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
            Double data = ((Number) objectValue).doubleValue();
            if (data.isNaN()) {
                jgen.writeString("NaN");
            } else if (data.isInfinite()) {
                jgen.writeString("Infinity");
            } else {
                jgen.writeNumber(((Number)objectValue).toString());
            }
        } else if (objectValue instanceof Boolean) {
            jgen.writeBoolean((Boolean)objectValue);
        } else {
            jgen.writeString(objectValue.toString());
        }
    }
}
