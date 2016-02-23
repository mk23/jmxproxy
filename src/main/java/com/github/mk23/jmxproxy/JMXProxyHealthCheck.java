package com.github.mk23.jmxproxy;

import com.github.mk23.jmxproxy.jmx.ConnectionManager;

import com.codahale.metrics.health.HealthCheck;

/**
 * <p>Service health check.</p>
 *
 * Checks the state of the {@link ConnectionManager}.
 *
 * @since   2015-05-11
 * @author  mk23
 * @version 3.2.0
 */
public class JMXProxyHealthCheck extends HealthCheck {
    /**
     * Lifecycle {@link ConnectionManager} object that determines
     * the overall service status.
     */
    private final ConnectionManager manager;

    /**
     * <p>Default constructor.</p>
     *
     * Uses the {@link ConnectionManager} object to determine
     * service status when a health check is requested.
     *
     * @param manager of the endpoint cache.
     */
    public JMXProxyHealthCheck(final ConnectionManager manager) {
        this.manager = manager;
    }

    /** {@inheritDoc} */
    @Override
    protected final Result check() throws Exception {
        if (!manager.isStarted()) {
            return Result.unhealthy("connection manager is not started");
        }
        return Result.healthy();
    }
}
