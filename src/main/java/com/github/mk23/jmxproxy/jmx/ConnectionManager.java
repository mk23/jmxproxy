package com.github.mk23.jmxproxy.jmx;

import com.github.mk23.jmxproxy.JMXProxyConfiguration.JMXProxyApplicationConfiguration;
import com.github.mk23.jmxproxy.core.Host;

import io.dropwizard.lifecycle.Managed;

import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;
import javax.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionManager implements Managed {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);

    private final JMXProxyApplicationConfiguration config;

    private Map<String, ConnectionWorker> hosts;
    private ScheduledExecutorService purge;

    private boolean started = false;

    public ConnectionManager(JMXProxyApplicationConfiguration config) {
        this.config = config;

        hosts = new HashMap<String, ConnectionWorker>();
        purge = Executors.newSingleThreadScheduledExecutor();
    }

    public JMXProxyApplicationConfiguration getConfiguration() {
        return config;
    }

    public Host getHost(String host) throws WebApplicationException {
        return getHost(host, null);
    }

    public Host getHost(String host, ConnectionCredentials auth) throws WebApplicationException {
        if (!config.getAllowedEndpoints().isEmpty() && !config.getAllowedEndpoints().contains(host)) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        try {
            synchronized (hosts) {
                if (hosts.containsKey(host) && !hosts.get(host).checkCredentials(auth)) {
                    LOG.info("resetting credentials for " + host);
                    hosts.remove(host).shutdown();
                }

                if (!hosts.containsKey(host)) {
                    LOG.info("creating new worker for " + host);
                    hosts.put(host, new ConnectionWorker(host, auth, config.getCacheDuration().toMilliseconds(), config.getHistorySize()));
                }
            }

            return hosts.get(host).getHost();
        } catch (SecurityException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    public boolean isStarted() {
        return started;
    }

    public void start() {
        LOG.info("starting jmx connection manager");

        LOG.debug("allowedEndpoints: " + config.getAllowedEndpoints().size());
        for (String ae : config.getAllowedEndpoints()) {
            LOG.debug("    " + ae);
        }

        purge.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                LOG.debug("begin expiring stale hosts");
                synchronized (hosts) {
                    for (Map.Entry<String, ConnectionWorker>hostEntry : hosts.entrySet()) {
                        if (hostEntry.getValue().isExpired(config.getAccessDuration().toMilliseconds())) {
                            LOG.debug("purging " + hostEntry.getKey());
                            hosts.remove(hostEntry.getKey()).shutdown();
                        }
                    }
                }
                LOG.debug("end expiring stale hosts");
            }
        }, config.getCleanInterval(), config.getCleanInterval(), TimeUnit.MINUTES);
        started = true;
    }

    public void stop() {
        LOG.info("stopping jmx connection manager");
        purge.shutdown();
        synchronized (hosts) {
            for (Map.Entry<String, ConnectionWorker>hostEntry : hosts.entrySet()) {
                LOG.debug("purging " + hostEntry.getKey());
                hosts.remove(hostEntry.getKey()).shutdown();
            }
        }
        hosts.clear();
        started = false;
    }
}
