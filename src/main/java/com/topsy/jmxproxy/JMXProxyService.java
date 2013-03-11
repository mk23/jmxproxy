package com.topsy.jmxproxy;

import com.topsy.jmxproxy.jmx.ConnectionManager;
import com.topsy.jmxproxy.JMXProxyResource;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMXProxyService extends Service<JMXProxyConfiguration> {
    private static final Logger LOG = LoggerFactory.getLogger(JMXProxyService.class);

    public static void main(String[] args) throws Exception {
        LOG.info("starting jmxproxy service");
        new JMXProxyService().run(args);
    }

    @Override
    public void initialize(Bootstrap<JMXProxyConfiguration> bootstrap) {
        bootstrap.setName("jmxproxy");
    }

    @Override
    public void run(JMXProxyConfiguration configuration, Environment environment) {
        final ConnectionManager manager = new ConnectionManager();

        environment.manage(manager);
        environment.addResource(new JMXProxyResource(manager, configuration.getJMXProxyServiceConfiguration()));
        environment.addHealthCheck(new JMXProxyHealthCheck(manager));
    }
}
