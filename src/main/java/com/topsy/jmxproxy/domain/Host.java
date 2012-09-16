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

    @XmlAttribute
    private String host;

    @XmlElement
    private List<Domain> domains;

    public Host(String hostName) {
        domains = new ArrayList<Domain>();
        host = hostName;
    }

    public List<Domain> getDomains() {
        return domains;
    }

    public String getHost() {
        return host;
    }

    public Domain addDomain(String domainName) {
        Domain domain = new Domain(domainName);
        domains.add(domain);

        return domain;
    }
}
