package com.topsy.jmxproxy;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class JMXProxyConfiguration extends Configuration {
    @JsonSnakeCase
    public static class JMXProxyApplicationConfiguration {
        @Min(1)
        @JsonProperty
        private int cleanInterval = 1;

        @Min(0)
        @JsonProperty
        private int cacheDuration = 5;

        @Min(1)
        @JsonProperty
        private int accessDuration = 30;

        @JsonProperty
        private List<String> allowedEndpoints = new ArrayList<String>();

        public int getCleanInterval() {
            return cleanInterval;
        }
        public void setCleanInterval(int cleanInterval) {
            this.cleanInterval = cleanInterval;
        }

        public int getCacheDuration() {
            return cacheDuration;
        }
        public void setCacheDuration(int cacheDuration) {
            this.cacheDuration = cacheDuration;
        }

        public int getAccessDuration() {
            return accessDuration;
        }
        public void setAccessDuration(int accessDuration) {
            this.accessDuration = accessDuration;
        }

        public List<String> getAllowedEndpoints() {
            return allowedEndpoints;
        }
        public void setAllowedEndpoints(List<String> allowedEndpoints) {
            this.allowedEndpoints = allowedEndpoints;
        }
    }

    @Valid
    @NotNull
    @JsonProperty(value="jmxproxy")
    private JMXProxyApplicationConfiguration applicationConfiguration = new JMXProxyApplicationConfiguration();

    public JMXProxyApplicationConfiguration getApplicationConfiguration() {
        return applicationConfiguration;
    }
    public void setApplicationConfiguration(JMXProxyApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
    }
}
