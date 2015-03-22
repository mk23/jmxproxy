package com.topsy.jmxproxy;

import com.topsy.jmxproxy.jmx.ConnectionManager;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.server.AbstractServerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMXProxyApplication extends Application<JMXProxyConfiguration> {
    private static final Logger LOG = LoggerFactory.getLogger(JMXProxyApplication.class);

    public static void main(String[] args) throws Exception {
        LOG.info("starting jmxproxy service");
        new JMXProxyApplication().run(args);
    }

    @Override
    public String getName() {
        return "jmxproxy";
    }

    @Override
    public void initialize(Bootstrap<JMXProxyConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
    }

    @Override
    public void run(JMXProxyConfiguration configuration, Environment environment) {
        final ConnectionManager manager = new ConnectionManager(configuration.getApplicationConfiguration());
        final JMXProxyResource resource = new JMXProxyResource(manager);
        final JMXProxyHealthCheck healthCheck = new JMXProxyHealthCheck(manager);

        ((AbstractServerFactory) configuration.getServerFactory()).setJerseyRootPath("/jmxproxy/*");

        environment.lifecycle().manage(manager);
        environment.jersey().register(resource);
        environment.healthChecks().register("manager", healthCheck);
    }
}
