package com.topsy.jmxproxy.domain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

public class Domain {
    private static final Logger LOG = Logger.getLogger(Domain.class);

    @XmlAttribute(name="name")
    private String domainName;

    @XmlElement
    private List<String> mbeans;

    public Domain() {
        mbeans = new ArrayList<String>();
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public void addMBean(String mbean) {
        mbeans.add(mbean);
    }
}
