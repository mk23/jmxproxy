package com.github.mk23.jmxproxy.jmx;

import com.github.mk23.jmxproxy.core.History;
import com.github.mk23.jmxproxy.core.Host;
import com.github.mk23.jmxproxy.core.MBean;

import java.io.IOException;

import java.net.MalformedURLException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>JMX fetcher and tracker.</p>
 *
 * Creates a {@link ScheduledExecutorService} to periodically make to a {@link JMXConnector}
 * to a JMX endpoint.  At every execution, discovers and fetches all remote registered mbeans
 * and populates the local {@link Host} object with assicated {@link MBean}s and their
 * {@link com.github.mk23.jmxproxy.core.Attribute}.  Also maintains the {@link Host} access
 * time to allow reaping of unaccessed workers.
 *
 * @author  mk23
 * @since   2015-05-11
 * @version 3.2.0
 */
public class ConnectionWorker {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionWorker.class);

    private Host host = null;
    private ConnectionCredentials auth = null;

    private int historySize;

    private JMXServiceURL url;

    private long accessTime;

    private ScheduledExecutorService fetch;

    /**
     * <p>Default constructor.</p>
     *
     * Initializes the {@link JMXServiceURL} to the specified hostName
     * and starts the {@link ScheduledExecutorService} for periodically
     * connecting and fetching JMX objects.
     *
     * @param hostName host:port {@link String} JMX agent target.
     * @param auth optional {@link ConnectionCredentials} for the provided JMX agent or null if none.
     * @param cacheDuration period in milliseconds for how often to connect to the JMX agent.
     * @param historySize number of {@link com.github.mk23.jmxproxy.core.Attribute}s to keep per
     *     {@link MBean} {@link History}.
     *
     * @throws MalformedURLException if hostName isn't a valid host:port {@link String}.
     */
    public ConnectionWorker(
        final String hostName,
        final ConnectionCredentials auth,
        final long cacheDuration,
        final int historySize
    ) throws MalformedURLException {
        this.auth = auth;
        this.historySize = historySize;

        url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + hostName + "/jmxrmi");
        fetch = Executors.newSingleThreadScheduledExecutor();

        fetch.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                fetchJMXValues();
            }
        }, cacheDuration, cacheDuration, TimeUnit.MILLISECONDS);

        fetchJMXValues();
    }

    /**
     * <p>Getter for host.<p>
     *
     * Fetches the tracked {@link Host} object and resets the request access time.
     *
     * @return {@link Host} as populated by the most recent fetch operation.
     */
    public final synchronized Host getHost() {
        accessTime = System.currentTimeMillis();
        return host;
    }

    /**
     * <p>Verifies tracked credentials.</p>
     *
     * Checks if the tracked {@link ConnectionCredentials} have changed since worker instantiation.
     *
     * @param peer new request {@link ConnectionCredentials} to compare against.
     *
     * @return true if {@link ConnectionCredentials} are the same between requests, false otherwise.
     */
    public final boolean checkCredentials(final ConnectionCredentials peer) {
        return auth == peer || auth != null && auth.equals(peer) || peer != null && peer.equals(auth);
    }

    /**
     * <p>Checks expiration of this worker object.</p>
     *
     * Checks the last access time against the provided duration limit to determine if this worker
     * is expired and can be purged.
     *
     * @param accessDuration time in milliseconds of no access after which this worker is expired.
     *
     * @return true if this worker hasn't been accessed recently, false otherwise.
     */
    public final boolean isExpired(final long accessDuration) {
        return System.currentTimeMillis() - accessTime > accessDuration;
    }

    /**
     * <p>Shuts down the scheduled fetcher.</p>
     *
     * Signals the {@link ScheduledExecutorService} to shutdown for process termination cleanup.
     */
    public final void shutdown() {
        if (!fetch.isShutdown()) {
            fetch.shutdown();
        }
    }

    private synchronized void fetchJMXValues() throws SecurityException {
        JMXConnector connection = null;
        MBeanServerConnection server = null;

        Map<String, Object> environment = new HashMap<String, Object>();
        if (auth != null) {
            environment.put(JMXConnector.CREDENTIALS, new String[]{auth.getUsername(), auth.getPassword()});
        }

        try {
            LOG.debug("connecting to mbean server " + url);

            connection = JMXConnectorFactory.connect(url, environment);
            server = connection.getMBeanServerConnection();

            if (host == null) {
                host = new Host();
            }

            Set<String> freshMBeans = new HashSet<String>();

            for (ObjectName mbeanName : server.queryNames(null, null)) {
                LOG.debug("discovered mbean " + mbeanName);
                freshMBeans.add(mbeanName.toString());

                MBean mbean = host.addMBean(mbeanName.toString());
                try {
                    for (MBeanAttributeInfo attributeObject : server.getMBeanInfo(mbeanName).getAttributes()) {
                        if (attributeObject.isReadable()) {
                            try {
                                History history = mbean.addHistory(attributeObject.getName(), historySize);
                                history.addAttributeValue(server.getAttribute(mbeanName, attributeObject.getName()));
                            } catch (java.lang.NullPointerException e) {
                                LOG.error("failed to add attribute " + attributeObject.toString() + ": " + e);
                            } catch (java.rmi.UnmarshalException e) {
                                LOG.error("failed to add attribute " + attributeObject.toString() + ": " + e);
                            } catch (javax.management.AttributeNotFoundException e) {
                                LOG.error("failed to add attribute " + attributeObject.toString() + ": " + e);
                            } catch (javax.management.MBeanException e) {
                                LOG.error("failed to add attribute " + attributeObject.toString() + ": " + e);
                            } catch (javax.management.RuntimeMBeanException e) {
                                LOG.error("failed to add attribute " + attributeObject.toString() + ": " + e);
                            }
                        }
                    }
                } catch (javax.management.InstanceNotFoundException e) {
                    LOG.error("failed to get mbean info for " + mbeanName, e);
                } catch (javax.management.IntrospectionException e) {
                    LOG.error("failed to get mbean info for " + mbeanName, e);
                } catch (javax.management.ReflectionException e) {
                    LOG.error("failed to get mbean info for " + mbeanName, e);
                }
            }

            Set<String> staleMBeans = new HashSet<String>(host.getMBeans());
            staleMBeans.removeAll(freshMBeans);
            for (String mbeanName : staleMBeans) {
                host.removeMBean(mbeanName);
                LOG.debug("removed stale mbean " + mbeanName);
            }
        } catch (IOException e) {
            host = null;
            LOG.error("communication failure with " + url, e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    LOG.debug("disconnected from " + url);
                } catch (IOException e) {
                    LOG.error("failed to disconnect from " + url, e);
                } finally {
                    connection = null;
                }
            }
        }
    }
}
