package com.topsy.jmxproxy.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Host implements JsonSerializable {
    private static final Logger LOG = LoggerFactory.getLogger(Host.class);

    private Map<String, MBean> mbeans;
    private Map<String, List<String>> domains;
    private boolean includeDomains = false;

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

    public void toggleDomains(boolean includeDomains) {
        this.includeDomains = includeDomains;
    }

    public void serialize(JsonGenerator jgen, SerializerProvider sp) throws IOException, JsonProcessingException {
        buildJson(jgen);
    }

    public void serializeWithType(JsonGenerator jgen, SerializerProvider sp, TypeSerializer ts) throws IOException, JsonProcessingException {
        buildJson(jgen);
    }

    public void buildJson(JsonGenerator jgen) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        if (includeDomains) {
            LOG.info("including domains");
            for (Map.Entry<String, List<String>>domainEntry : domains.entrySet()) {
                jgen.writeObjectFieldStart(domainEntry.getKey());
                for (String mbeanName : domainEntry.getValue()) {
                    jgen.writeObjectField(mbeanName, mbeans.get(mbeanName));
                }
                jgen.writeEndObject();
            }
        } else {
            LOG.info("excluding domains");
            for (Map.Entry<String, MBean>mbeanEntry : mbeans.entrySet()) {
                jgen.writeObjectField(mbeanEntry.getKey(), mbeanEntry.getValue());
            }
        }
        jgen.writeEndObject();
    }
}
