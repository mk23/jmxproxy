package com.topsy.jmxproxy.domain;

import com.topsy.jmxproxy.domain.MBean;

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
    private List<MBean> mbeans;

    public Host() {
        mbeans = new ArrayList<MBean>();
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public MBean addMBean(String mbeanName) {
        MBean mbean = new MBean();
        mbean.setMBeanName(mbeanName);

        mbeans.add(mbean);

        return mbean;
    }
}
