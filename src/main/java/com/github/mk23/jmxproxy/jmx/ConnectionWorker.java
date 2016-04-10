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

import java.util.concurrent.CountDownLatch;
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
 * @since   2015-05-11
 * @author  mk23
 * @version 3.2.0
 */
public class ConnectionWorker {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionWorker.class);

    private Host host;
    private ConnectionCredentials authCreds;

    private boolean authValid;
    private IOException connError;

    private final Object fetchLock;
    private final int historySize;
    private final long cacheDuration;

    private final JMXServiceURL url;

    private long accessTime;

    private ScheduledExecutorService pollerSvc;

    /**
     * <p>Default constructor.</p>
     *
     * Initializes the {@link JMXServiceURL} to the specified hostName
     * and starts the {@link ScheduledExecutorService} for periodically
     * connecting and fetching JMX objects.
     *
     * @param hostName host:port {@link String} JMX agent target.
     * @param cacheDuration period in milliseconds for how often to connect to the JMX agent.
     * @param historySize number of {@link com.github.mk23.jmxproxy.core.Attribute}s to keep per
     *     {@link MBean} {@link History}.
     *
     * @throws MalformedURLException if the specified host is not a valid host:port {@link String}.
     */
    public ConnectionWorker(
        final String hostName,
        final long cacheDuration,
        final int historySize
    ) throws MalformedURLException {
        this.historySize = historySize;
        this.cacheDuration = cacheDuration;

        fetchLock = new Object();
        url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + hostName + "/jmxrmi");
    }

    /**
     * <p>Getter for host.<p>
     *
     * Fetches the tracked {@link Host} object and resets the request access time.
     *
     * @param testCreds optional {@link ConnectionCredentials} for the provided JMX agent or null if none.
     *
     * @return {@link Host} as populated by the most recent fetch operation.
     *
     * @throws IOException if the specified host is unreachable.
     * @throws SecurityException if the specified credentials to the host are incorrect.
     */
    public final Host getHost(final ConnectionCredentials testCreds) throws IOException, SecurityException {
        if (host == null) {
            final CountDownLatch ready = new CountDownLatch(1);

            shutdown();

            host = new Host();
            authCreds = (testCreds != null) ? testCreds : new ConnectionCredentials();

            pollerSvc = Executors.newSingleThreadScheduledExecutor();
            pollerSvc.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        fetchJMXValues();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ready.countDown();
                }
            }, 0, cacheDuration, TimeUnit.MILLISECONDS);

            try {
                ready.await();
            } catch (InterruptedException e) {
                LOG.error("unable to finish first run", e);
            }
        }

        if (connError != null) {
            throw connError;
        } else if (!authCreds.equals(testCreds) && authValid || !authValid) {
            throw new SecurityException();
        } else {
            synchronized (fetchLock) {
                accessTime = System.currentTimeMillis();
                return host;
            }
        }
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
     * <p>Stops the scheduled fetcher.</p>
     *
     * Signals the {@link ScheduledExecutorService} to shutdown for process termination cleanup.
     */
    public final void shutdown() {
        if (pollerSvc != null && !pollerSvc.isShutdown()) {
            pollerSvc.shutdown();
        }
    }

    private void fetchJMXValues() {
        JMXConnector connection = null;
        MBeanServerConnection server = null;

        Map<String, Object> environment = new HashMap<String, Object>();
        if (authCreds != null) {
            environment.put(JMXConnector.CREDENTIALS, new String[]{authCreds.getUsername(), authCreds.getPassword()});
        }

        try {
            LOG.debug("connecting to mbean server " + url);

            synchronized (fetchLock) {
                connection = JMXConnectorFactory.connect(url, environment);
                server = connection.getMBeanServerConnection();

                authValid = true;
                connError = null;

                Set<String> freshMBeans = new HashSet<String>();

                for (ObjectName mbeanName : server.queryNames(null, null)) {
                    LOG.debug("discovered mbean " + mbeanName);
                    freshMBeans.add(mbeanName.toString());

                    MBean mbean = host.addMBean(mbeanName.toString());
                    try {
                        for (MBeanAttributeInfo attributeObject : server.getMBeanInfo(mbeanName).getAttributes()) {
                            if (attributeObject.isReadable()) {
                                try {
                                    Object attribute = server.getAttribute(mbeanName, attributeObject.getName());

                                    History history = mbean.addHistory(attributeObject.getName(), historySize);
                                    history.addAttributeValue(attribute);
                                } catch (java.lang.NullPointerException e) {
                                    LOG.error("failed to add attribute " + attributeObject.toString() + ": " + e);
                                } catch (java.rmi.UnmarshalException e) {
                                    LOG.error("failed to add attribute " + attributeObject.toString() + ": " + e);
                                } catch (javax.management.AttributeNotFoundException e) {
                                    LOG.error("failed to add attribute " + attributeObject.toString() + ": " + e);
                                } catch (javax.management.MBeanException e) {
                                    LOG.error("failed to add attribute " + attributeObject.toString() + ": " + e);
                                } catch (javax.management.RuntimeMBeanException e) {
                                    if (!(e.getCause() instanceof UnsupportedOperationException)) {
                                        LOG.error("failed to add attribute " + attributeObject.toString() + ": " + e);
                                    }
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
            }
        } catch (IOException e) {
            host = null;
            connError = e;
            LOG.error("communication failure with " + url, e);
        } catch (SecurityException e) {
            host = null;
            authValid = false;
            LOG.error("invalid credentials for " + url, e);
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
