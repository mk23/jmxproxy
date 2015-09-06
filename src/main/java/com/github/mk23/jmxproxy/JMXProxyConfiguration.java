package com.github.mk23.jmxproxy;

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

        @Min(1)
        @JsonProperty
        private int historySize = 1;

        @JsonProperty
        private List<String> allowedEndpoints = new ArrayList<String>();

        public long getCleanInterval() {
            return cleanInterval;
        }
        public JMXProxyApplicationConfiguration setCleanInterval(long cleanInterval) {
            this.cleanInterval = cleanInterval;
            return this;
        }

        public long getCacheDuration() {
            return cacheDuration;
        }
        public JMXProxyApplicationConfiguration setCacheDuration(long cacheDuration) {
            this.cacheDuration = cacheDuration;
            return this;
        }

        public long getAccessDuration() {
            return accessDuration;
        }
        public JMXProxyApplicationConfiguration setAccessDuration(long accessDuration) {
            this.accessDuration = accessDuration;
            return this;
        }

        public int getHistorySize() {
            return historySize;
        }
        public JMXProxyApplicationConfiguration setHistorySize(int historySize) {
            this.historySize = historySize;
            return this;
        }

        public List<String> getAllowedEndpoints() {
            return allowedEndpoints;
        }
        public JMXProxyApplicationConfiguration setAllowedEndpoints(List<String> allowedEndpoints) {
            this.allowedEndpoints = allowedEndpoints;
            return this;
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
