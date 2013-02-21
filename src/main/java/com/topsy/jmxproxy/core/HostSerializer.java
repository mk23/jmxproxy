package com.topsy.jmxproxy.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.SerializerBase;

public class HostSerializer extends SerializerBase<Host> {
    public HostSerializer() {
        super(Host.class, true);
    }

    @Override
    public void serialize(Host value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        for (Map.Entry<String, List<String>>domainEntry : value.getDomains().entrySet()) {
            jgen.writeObjectFieldStart(domainEntry.getKey());
            for (String mbeanName : domainEntry.getValue()) {
                jgen.writeObjectField(mbeanName, value.getMBeans().get(mbeanName));
            }
            jgen.writeEndObject();
        }
        jgen.writeEndObject();
    }
}
