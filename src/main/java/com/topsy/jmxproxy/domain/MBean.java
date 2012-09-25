package com.topsy.jmxproxy.domain;

import com.topsy.jmxproxy.domain.Attribute;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.log4j.Logger;

public class MBean {
	private static final Logger LOG = Logger.getLogger(MBean.class);

	@XmlAttribute(name="mbean")
	private String mbeanName;

	@XmlElement
	private List<Attribute> attributes;

	public MBean() {
		this.attributes = new ArrayList<Attribute>();
	}

	public void setMBeanName(String mbeanName) {
		this.mbeanName = mbeanName;
	}

	public Attribute addAttribute(String attributeName) {
		Attribute attribute = new Attribute();
		attribute.setAttributeName(attributeName);

		attributes.add(attribute);

		return attribute;
	}
    /*
    private Map<String, Attribute> attributes;

    public MBean(MBeanAttributeInfo[] attributes) {
        this.attributes = new HashMap<String, Attribute>();

        for (MBeanAttributeInfo attribute : attributes) {
            if (attribute.isReadable()) {
                this.attributes.put(attribute.getName(), new Attribute());
            }
        }
    }

    public String[] getAttributes() {
        return (String[])attributes.keySet().toArray(new String[0]);
    }

    public boolean hasAttribute(String attribute) {
        return attributes.containsKey(attribute);
    }

    public Attribute getAttribute(String attribute) {
        return attributes.get(attribute);
    }
    */
}


