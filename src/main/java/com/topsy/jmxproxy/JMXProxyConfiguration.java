package com.topsy.jmxproxy;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.yammer.dropwizard.config.Configuration;

public class JMXProxyConfiguration extends Configuration {
    @JsonProperty(value="jmxproxy")
    private JMXProxyServiceConfiguration configuration = new JMXProxyServiceConfiguration();

    public JMXProxyServiceConfiguration getJMXProxyServiceConfiguration() {
        return configuration;
    }

}
