package com.github.mk23.jmxproxy.conf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dropwizard.jackson.JsonSnakeCase;

import io.dropwizard.util.Duration;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;

/**
 * <p>Application configuration.</p>
 *
 * Maintains application configuration fields. The application initializer
 * creates and marshals the object from JSON or Yaml config file. Setters
 * may be chained together for configuration building.
 *
 * <p>For example:</p>
 *
 * <code>
 * AppConfig cfg = new AppConfig()
 *     .setCleanInterval(Duration.minutes(5))
 *     .setCacheDuration(Duration.minutes(10))
 *     .setHistorySize(20);
 * </code>
 *
 * @see <a href="http://dropwizard.github.io/dropwizard/0.9.2/dropwizard-util/apidocs/io/dropwizard/util/Duration.html">io.dropwizard.util.Duration</a>
 *
 * @since   2016-01-28
 * @author  mk23
 * @version 3.3.6
 */
@JsonSnakeCase
public class AppConfig {
    /** Default clean interval minutes. */
    private static final int DEFAULT_CLEAN_INTERVAL = 1;
    /** Default cache duration minutes. */
    private static final int DEFAULT_CACHE_DURATION = 5;
    /** Default access duration minutes. */
    private static final int DEFAULT_ACCESS_DURATION = 30;
    /** Default jmx connect timeout milliseconds. */
    private static final int DEFAULT_CONNECT_TIMEOUT = 10000;

    /**
     * Configuration for how often to run the task that finds and
     * purges stale endpoints.
     */
    @Valid
    @JsonProperty
    @JsonDeserialize(using = DurationDeserializer.class)
    @JsonSerialize(using = DurationSerializer.class)
    private Duration cleanInterval = Duration.minutes(DEFAULT_CLEAN_INTERVAL);

    /**
     * Configuration for how often to reconnect to cached endpoints
     * and scrape available attributes.
     */
    @Valid
    @JsonProperty
    @JsonDeserialize(using = DurationDeserializer.class)
    @JsonSerialize(using = DurationSerializer.class)
    private Duration cacheDuration = Duration.minutes(DEFAULT_CACHE_DURATION);

    /**
     * Configuration for how long an endpoint goes unaccessed before
     * the purger tasks removes it from the cache.
     */
    @Valid
    @JsonProperty
    @JsonDeserialize(using = DurationDeserializer.class)
    @JsonSerialize(using = DurationSerializer.class)
    private Duration accessDuration = Duration.minutes(DEFAULT_ACCESS_DURATION);

    /**
     * Configuration for how long to wait for a JMX connection to complete
     * before declaring the endpoint as inaccessible.
     */
    @Valid
    @JsonProperty
    @JsonDeserialize(using = DurationDeserializer.class)
    @JsonSerialize(using = DurationSerializer.class)
    private Duration connectTimeout = Duration.minutes(DEFAULT_CONNECT_TIMEOUT);

    /**
     * Configuration for how many fetched attribute values to keep for
     * each cached endpoint.
     */
    @Min(1)
    @JsonProperty
    private int historySize = 1;

    /**
     * Configuration for whitelisted endpoints, allowing all when this
     * is empty.
     */
    @JsonProperty
    private List<String> allowedEndpoints = new ArrayList<String>();

    /**
     * <p>Getter for cleanInterval.</p>
     *
     * Configuration for how often to run the task that finds and
     * purges stale endpoints.
     *
     * @return Configured clean interval.
     */
    public final Duration getCleanInterval() {
        return cleanInterval;
    }
    /**
     * <p>Setter for cleanInterval.</p>
     *
     * @param cleanInterval period at which the purger tasks operates.
     *
     * @return Modified AppConfig for setter chaining.
     */
    public final AppConfig setCleanInterval(final Duration cleanInterval) {
        this.cleanInterval = cleanInterval;
        return this;
    }

    /**
     * <p>Getter for cacheDuration.</p>
     *
     * Configuration for how often to reconnect to cached endpoints
     * and scrape available attributes.
     *
     * @return Configured cache duration.
     */
    public final Duration getCacheDuration() {
        return cacheDuration;
    }
    /**
     * <p>Setter for cacheDuration.</p>
     *
     * @param cacheDuration period at which the endpoint cache
     * refreshes.
     *
     * @return Modified AppConfig for setter chaining.
     */
    public final AppConfig setCacheDuration(final Duration cacheDuration) {
        this.cacheDuration = cacheDuration;
        return this;
    }

    /**
     * <p>Getter for accessDuration.</p>
     *
     * Configuration for how long an endpoint goes unaccessed before
     * the purger tasks removes it from the cache.
     *
     * @return Configured access duration.
     */
    public final Duration getAccessDuration() {
        return accessDuration;
    }
    /**
     * <p>Setter for accessDuration.</p>
     *
     * @param accessDuration time before an enpoint is removed
     * from the cache.
     *
     * @return Modified AppConfig for setter chaining.
     */
    public final AppConfig setAccessDuration(final Duration accessDuration) {
        this.accessDuration = accessDuration;
        return this;
    }

    /**
     * <p>Getter for connectTimeout.</p>
     *
     * Configuration for how long to wait for a JMX connection to complete
     * before declaring the endpoint as inaccessible.
     *
     * @return Configured connect timeout.
     */
    public final Duration getConnectTimeout() {
        return connectTimeout;
    }
    /**
     * <p>Setter for connectTimeout.</p>
     *
     * @param connectTimeout time before an abandoning a new JMX connection.
     *
     * @return Modified AppConfig for setter chaining.
     */
    public final AppConfig setConnectTimeout(final Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * <p>Getter for historySize.</p>
     *
     * Configuration for how many fetched attribute values to keep for
     * each cached endpoint.
     *
     * @return Configured history size.
     */
    public final int getHistorySize() {
        return historySize;
    }
    /**
     * <p>Setter for historySize.</p>
     *
     * @param historySize number of values to keep for all cached
     * attributes at every requested endpoint.
     *
     * @return Modified AppConfig for setter chaining.
     */
    public final AppConfig setHistorySize(final int historySize) {
        this.historySize = historySize;
        return this;
    }

    /**
     * <p>Getter for allowedEndpoints.</p>
     *
     * Configuration for whitelisted endpoints, allowing all when this
     * is empty.
     *
     * @return Configured {@link List} containing whitelisted endpoints.
     */
    public final List<String> getAllowedEndpoints() {
        return allowedEndpoints;
    }
    /**
     * <p>Setter for allowedEndpoints.</p>
     *
     * @param allowedEndpoints {@link List} containing whitelisted endpoints.
     *
     * @return Modified AppConfig for setter chaining.
     */
    public final AppConfig setAllowedEndpoints(final List<String> allowedEndpoints) {
        this.allowedEndpoints = allowedEndpoints;
        return this;
    }
}
