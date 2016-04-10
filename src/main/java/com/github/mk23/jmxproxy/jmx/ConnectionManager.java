package com.github.mk23.jmxproxy.jmx;

import com.github.mk23.jmxproxy.conf.AppConfig;
import com.github.mk23.jmxproxy.core.Host;

import io.dropwizard.lifecycle.Managed;

import java.net.MalformedURLException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;
import javax.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Main application lifecycle object that manages all JMX Agent Connections.</p>
 *
 * Implements the dropwizard lifecycle Managed interface to create the object responsible
 * for all application data management.  Creates workers for connecting to JMX endpoints and
 * handles requests for data retreival.  Controls purging unaccessed endpoints as well as
 * their runtime state.
 *
 * @see <a href="http://dropwizard.github.io/dropwizard/0.9.2/dropwizard-lifecycle/apidocs/io/dropwizard/lifecycle/Managed.html">io.dropwizard.lifecycle.Managed</a>
 *
 * @since   2015-05-11
 * @author  mk23
 * @version 3.2.0
 */
public class ConnectionManager implements Managed {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);

    private final AppConfig config;

    private final Map<String, ConnectionWorker> hosts;
    private final ScheduledExecutorService purge;

    private boolean started = false;

    /**
     * <p>Default constructor.</p>
     *
     * Called by the application initializer.  Creates the map of host:port {@link String}s to
     * associated {@link ConnectionWorker} instances.  Creates an instance of the unaccessed
     * endpoints purge thread.  Saves the provided applicaiton configuration for later retreival
     * request.
     *
     * @param config configuration as specified by the administrator at application invocation.
     */
    public ConnectionManager(final AppConfig config) {
        this.config = config;

        hosts = new HashMap<String, ConnectionWorker>();
        purge = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * <p>Getter for config.</p>
     *
     * Fetches the application run-time configuration object.
     *
     * @return application configuration.
     */
    public final AppConfig getConfiguration() {
        return config;
    }

    /**
     * <p>Getter for hosts.</p>
     *
     * Fetches the {@link Set} of {@link ConnectionWorker} name {@link String}s.
     *
     * @return {@link Set} of {@link ConnectionWorker} name {@link String}s.
     */
    public final Set<String> getHosts() {
        synchronized (hosts) {
            return hosts.keySet();
        }
    }

    /**
     * <p>Deleter for host.</p>
     *
     * Attempts to remove the specified host from the {@link ConnectionWorker} map store.
     *
     * @param host endpoint host:port {@link String}.
     *
     * @return true if the key is found in the map store.
     *
     * @throws WebApplicationException if key is not found in the map store.
     */
    public final boolean delHost(final String host) throws WebApplicationException {
        synchronized (hosts) {
            if (hosts.containsKey(host)) {
                LOG.debug("purging " + host);
                hosts.remove(host).shutdown();

                return true;
            } else {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }
    }

    /**
     * <p>Anonymous getter for host.</p>
     *
     * Fetches the specified {@link Host} with anonymous (null) credentials.
     * Equivalent to calling: <br>
     *
     * <code>return getHost(host, null);</code>
     *
     * @param host endpoint host:port {@link String}.
     *
     * @return {@link Host} object for the requested endpoint.
     *
     * @throws WebApplicationException if unauthorized, forbidden, or invalid endpoint.
     */
    public final Host getHost(final String host) throws WebApplicationException {
        return getHost(host, null);
    }

    /**
     * <p>Authenticated getter for host.</p>
     *
     * Fetches the specified {@link Host} with provided credentials.  Validates endpoint
     * access against a configured whitelist.  Validates provided credentials against
     * previous requests and if different, a new {@link ConnectionWorker} object is
     * instanciated and associated with the specified endpoint.  Lastly, if specified
     * endpoint is not yet in the map store, instanciates a new {@link ConnectionWorker}
     * and saves it for later retreival.
     *
     * @param host endpoint host:port {@link String}.
     * @param auth endpoint {@link ConnectionCredentials} or null for anonymous access.
     *
     * @return {@link Host} object for the requested endpoint.
     *
     * @throws WebApplicationException if
     *     <ul>
     *         <li>forbidden (not whitelisted)</li>
     *         <li>unauthized (incorrect credentials)</li>
     *         <li>bad request (malformed host:port)</li>
     *         <li>not found (any other exception)</li>
     *     </ul>
     */
    public final Host getHost(
        final String host,
        final ConnectionCredentials auth
    ) throws WebApplicationException {
        if (!config.getAllowedEndpoints().isEmpty() && !config.getAllowedEndpoints().contains(host)) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        try {
            synchronized (hosts) {
                if (!hosts.containsKey(host)) {
                    LOG.info("creating new worker for " + host);
                    hosts.put(host, new ConnectionWorker(
                        host,
                        config.getCacheDuration().toMilliseconds(),
                        config.getHistorySize()
                    ));

                }
                return hosts.get(host).getHost(auth);
            }
        } catch (MalformedURLException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (SecurityException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    /**
     * <p>Getter for started.</p>
     *
     * Used by the application health check to verify the manager start() method has been invoked.
     *
     * @return true if the manager was started, false otherwise.
     */
    public final boolean isStarted() {
        return started;
    }

    /**
     * <p>Handler for application startup.</p>
     *
     * Starts the unaccessed endpoint purge thread at application initialization.
     */
    public final void start() {
        LOG.info("starting jmx connection manager");

        LOG.debug("allowedEndpoints: " + config.getAllowedEndpoints().size());
        for (String ae : config.getAllowedEndpoints()) {
            LOG.debug("    " + ae);
        }

        long cleanInterval = config.getCleanInterval().toMilliseconds();

        purge.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                LOG.debug("begin expiring stale hosts");
                synchronized (hosts) {
                    for (Map.Entry<String, ConnectionWorker> hostEntry : hosts.entrySet()) {
                        if (hostEntry.getValue().isExpired(config.getAccessDuration().toMilliseconds())) {
                            LOG.debug("purging " + hostEntry.getKey());
                            hosts.remove(hostEntry.getKey()).shutdown();
                        }
                    }
                }
                LOG.debug("end expiring stale hosts");
            }
        }, cleanInterval, cleanInterval, TimeUnit.MILLISECONDS);

        started = true;
    }

    /**
     * <p>Handler for application shutdown.</p>
     *
     * Stops the purge thread and all currently tracked {@link ConnectionWorker} instances.
     */
    public final void stop() {
        LOG.info("stopping jmx connection manager");
        purge.shutdown();
        synchronized (hosts) {
            for (Map.Entry<String, ConnectionWorker> hostEntry : hosts.entrySet()) {
                LOG.debug("purging " + hostEntry.getKey());
                hosts.remove(hostEntry.getKey()).shutdown();
            }
        }
        hosts.clear();
        started = false;
    }
}
