package com.topsy.jmxproxy.jmx;

import com.topsy.jmxproxy.core.Host;

import com.yammer.dropwizard.lifecycle.Managed;

import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionManager implements Managed {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);

    private final JMXProxyServiceConfiguration config;

    private Map<String, ConnectionWorker> hosts;
    private ScheduledExecutorService purge;

    private boolean started = false;

    public ConnectionManager(JMXProxyServiceConfiguration config) {
        this.config = config;

        hosts = new HashMap<String, ConnectionWorker>();
        purge = Executors.newSingleThreadScheduledExecutor();
    }

    public Host getHost(String host) throws Exception {
        synchronized (hosts) {
            if (!hosts.containsKey(host)) {
                LOG.info("creating new worker for " + host);
                hosts.put(host, new ConnectionWorker(host));
            }
        }

        return hosts.get(host).getHost();
    }

    public boolean isStarted() {
        return started;
    }

    public void start() {
        LOG.info("starting jmx connection manager");

        purge.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                LOG.debug("begin expiring stale hosts");
                synchronized (hosts) {
                    for (Map.Entry<String, ConnectionWorker>hostEntry : hosts.entrySet()) {
                        if (hostEntry.getValue().isExpired()) {
                            LOG.debug("purging " + hostEntry.getKey());
                            hosts.remove(hostEntry.getKey());
                        }
                    }
                }
                LOG.debug("end expiring stale hosts");
            }
        }, 1, 1, TimeUnit.MINUTES);
        started = true;
    }

    public void stop() {
        LOG.info("stopping jmx connection manager");
        purge.shutdown();
        hosts.clear();
        started = false;
    }
}
