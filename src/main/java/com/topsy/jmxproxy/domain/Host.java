package com.topsy.jmxproxy.domain;

import com.topsy.jmxproxy.domain.MBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializable;
import org.codehaus.jackson.map.SerializerProvider;

public class Host implements JsonSerializable{
    private Map<String, MBean> mbeans;
    private Map<String, List<String>> domains;

    public Host() {
        this.mbeans = new HashMap<String, MBean>();
        this.domains = new HashMap<String, List<String>>();
    }

    public MBean addMBean(String domain, String mbeanName) {
        if (!domains.containsKey(domain)) {
            domains.put(domain, new ArrayList<String>());
        }
        domains.get(domain).add(mbeanName);

        MBean mbean = new MBean();
        mbeans.put(mbeanName, mbean);

        return mbean;
    }

    @Override
    public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        for (Map.Entry<String, List<String>>domainEntry : domains.entrySet()) {
            jgen.writeObjectFieldStart(domainEntry.getKey());
            for (String mbeanName : domainEntry.getValue()) { 
                jgen.writeObjectField(mbeanName, mbeans.get(mbeanName));
            }
            jgen.writeEndObject();
        }
        jgen.writeEndObject();
    }
}
