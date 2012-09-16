package com.topsy.jmxproxy.domain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

@XmlRootElement
public class Domain {
    private static final Logger LOG = Logger.getLogger(Domain.class);

    @XmlAttribute
    private String domain;

    @XmlElement
    private List<String> mbeans;

    public Domain(String domainName) {
        domain = domainName;
        mbeans = new ArrayList<String>();
    }

    public String getDomain() {
        return domain;
    }

    public List<String> getMBeans() {
        return mbeans;
    }

    public void addMBean(String mbean) {
        mbeans.add("test blah");
    }
}
