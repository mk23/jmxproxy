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
 *
 * Configures application from {@link MainConfig}, initializes, and starts the service.
 *
 * @see <a href="http://dropwizard.github.io/dropwizard/0.9.3/dropwizard-core/apidocs/io/dropwizard/Application.html">io.dropwizard.Application</a>
 * @see <a href="http://dropwizard.github.io/dropwizard/0.9.3/dropwizard-core/apidocs/io/dropwizard/setup/Bootstrap.html">io.dropwizard.setup.Bootstrap</a>
 * @see <a href="http://dropwizard.github.io/dropwizard/0.9.3/dropwizard-core/apidocs/io/dropwizard/setup/Environment.html">io.dropwizard.setup.Environment</a>
 *
 * @since   2015-05-11
 * @author  mk23
 * @version 3.2.0
 */
public class JMXProxyApplication extends Application<MainConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(JMXProxyApplication.class);

    /**
     * <p>main application entrypoint.</p>
     *
     * Starts main application.
     *
     * @param args an array of {@link String} command-line parameters.
     *
     * @throws Exception if initialization fails.
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
