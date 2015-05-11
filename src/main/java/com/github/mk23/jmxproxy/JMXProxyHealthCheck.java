package com.github.mk23.jmxproxy;

import com.github.mk23.jmxproxy.jmx.ConnectionManager;

import com.codahale.metrics.health.HealthCheck;

public class JMXProxyHealthCheck extends HealthCheck {
    private final ConnectionManager manager;

    public JMXProxyHealthCheck(ConnectionManager manager) {
        this.manager = manager;
    }

    @Override
    protected Result check() throws Exception {
        if (!manager.isStarted()) {
            return Result.unhealthy("connection manager is not started");
        }
        return Result.healthy();
    }
}
