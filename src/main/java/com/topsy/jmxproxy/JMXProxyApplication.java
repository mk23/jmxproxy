package com.topsy.jmxproxy;

import com.topsy.jmxproxy.jmx.ConnectionManager;
import com.topsy.jmxproxy.JMXProxyResource;

import io.dropwizard.Application;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMXProxyApplication extends Application<JMXProxyConfiguration> {
    private static final Logger LOG = LoggerFactory.getLogger(JMXProxyApplication.class);

    public static void main(String[] args) throws Exception {
        LOG.info("starting jmxproxy service");
        new JMXProxyApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<JMXProxyConfiguration> bootstrap) {
        bootstrap.setName("jmxproxy");
        bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
    }

    @Override
    public void run(JMXProxyConfiguration configuration, Environment environment) {
        final ConnectionManager manager = new ConnectionManager(configuration.getApplicationConfiguration());

        environment.manage(manager);
        environment.addResource(new JMXProxyResource(manager));
        environment.addHealthCheck(new JMXProxyHealthCheck(manager));
    }
}
