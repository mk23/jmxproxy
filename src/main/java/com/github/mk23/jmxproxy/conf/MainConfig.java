package com.github.mk23.jmxproxy.conf;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

import javax.validation.Valid;

/**
 * <p>Main top-level configuration.</p>
 *
 * Maintains the top-level application configuration object. The
 * application initializer creates and marshals the object from
 * JSON or Yaml config file.
 *
 * @since   2016-01-28
 * @author  mk23
 * @version 3.2.1
 */
public class MainConfig extends Configuration {
    /**
     * Application service config.
     */
    @Valid
    @JsonProperty("jmxproxy")
    private AppConfig appConfig = new AppConfig();

    /**
     * <p>Getter for appConfig.</p>
     *
     * Application service config.
     *
     * @return Application subsection from the full configuration file.
     */
    public final AppConfig getAppConfig() {
        return appConfig;
    }
    /**
     * <p>Setter for appConfig.</p>
     *
     * @param appConfig Application subsection from the full configuration file.
     */
    public final void setAppConfig(final AppConfig appConfig) {
        this.appConfig = appConfig;
    }
}
