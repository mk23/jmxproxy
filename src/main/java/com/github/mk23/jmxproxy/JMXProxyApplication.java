package com.github.mk23.jmxproxy;

import com.github.mk23.jmxproxy.conf.MainConfig;
import com.github.mk23.jmxproxy.jmx.ConnectionManager;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.server.AbstractServerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>JMXProxy main application.</p>
 * Configures, initializes, and starts the service.
 *
 * @author  mk23
 * @since   2015-05-11
 */
public class JMXProxyApplication extends Application<MainConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(JMXProxyApplication.class);

    /**
     * <p>main.</p>
     * Starts main application.
     *
     * @param args an array of {@link java.lang.String} objects.
     * @throws java.lang.Exception if any.
     */
    public static void main(final String[] args) throws Exception {
        LOG.info("starting jmxproxy service");
        new JMXProxyApplication().run(args);
    }

    /** {@inheritDoc} */
    @Override
    public final String getName() {
        return "jmxproxy";
    }

    /** {@inheritDoc} */
    @Override
    public final void initialize(final Bootstrap<MainConfig> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
    }

    /** {@inheritDoc} */
    @Override
    public final void run(final MainConfig configuration, final Environment environment) {
        final ConnectionManager manager = new ConnectionManager(configuration.getAppConfig());
        final JMXProxyResource resource = new JMXProxyResource(manager);
        final JMXProxyHealthCheck healthCheck = new JMXProxyHealthCheck(manager);

        ((AbstractServerFactory) configuration.getServerFactory()).setJerseyRootPath("/jmxproxy/*");

        environment.lifecycle().manage(manager);
        environment.jersey().register(resource);
        environment.healthChecks().register("manager", healthCheck);
    }
}
