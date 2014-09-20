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
        private long cleanInterval = 1;

        @Min(0)
        @JsonProperty
        private long cacheDuration = 5;

        @Min(1)
        @JsonProperty
        private long accessDuration = 30;

        @JsonProperty
        private List<String> allowedEndpoints = new ArrayList<String>();

        public long getCleanInterval() {
            return cleanInterval;
        }
        public void setCleanInterval(long cleanInterval) {
            this.cleanInterval = cleanInterval;
        }

        public long getCacheDuration() {
            return cacheDuration;
        }
        public void setCacheDuration(long cacheDuration) {
            this.cacheDuration = cacheDuration;
        }

        public long getAccessDuration() {
            return accessDuration;
        }
        public void setAccessDuration(long accessDuration) {
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
