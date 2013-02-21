package com.topsy.jmxproxy;

import com.topsy.jmxproxy.jmx.ConnectionManager;

import com.yammer.metrics.core.HealthCheck;

public class JMXProxyHealthCheck extends HealthCheck {
    private final ConnectionManager manager;

    public JMXProxyHealthCheck(ConnectionManager manager) {
        super("manager");
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
