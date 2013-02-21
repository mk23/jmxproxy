package com.topsy.jmxproxy.core;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.SerializerBase;

public class MBeanSerializer extends SerializerBase<MBean> {
    public MBeanSerializer() {
        super(MBean.class, true);
    }

    @Override
    public void serialize(MBean value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        for (Map.Entry<String, Attribute>attributeEntry : value.getAttributes().entrySet()) {
            jgen.writeObjectField(attributeEntry.getKey(), attributeEntry.getValue());
        }
        jgen.writeEndObject();
    }
}
