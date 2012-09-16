package com.topsy.jmxproxy.domain;

import com.topsy.jmxproxy.domain.Domain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

@XmlRootElement
public class Host {
    private static final Logger LOG = Logger.getLogger(Host.class);

    @XmlAttribute(name="host")
    private String hostName;

    @XmlElement
    private List<Domain> domains;

    public Host() {
        domains = new ArrayList<Domain>();
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Domain addDomain(String domainName) {
        Domain domain = new Domain();
        domain.setDomainName(domainName);

        domains.add(domain);

        return domain;
    }
}
