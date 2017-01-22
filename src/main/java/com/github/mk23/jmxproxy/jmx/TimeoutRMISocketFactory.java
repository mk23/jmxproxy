package com.github.mk23.jmxproxy.jmx;

import java.io.IOException;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.ServerSocket;

import java.rmi.server.RMISocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Remote Method Invocation socket factory with connection timeout.</p>
 *
 * Extends RMISocketFactory to return a socket that implements a connection timeout,
 * preventing black-holed backend agents from piling up connections in the server.
 *
 * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/net/Socket.html">java.net.Socket</a>
 * @see <a href="https://docs.oracle.com/javase/7/docs/api/java/rmi/server/RMISocketFactory.html">java.rmi.server.RMISocketFactory</a>
 *
 * @since   2017-01-20
 * @author  mk23
 * @version 3.3.6
 */
public class TimeoutRMISocketFactory extends RMISocketFactory {
    private static final Logger LOG = LoggerFactory.getLogger(TimeoutRMISocketFactory.class);

    private int timeout;

    /**
     * <p>Default constructor.</p>
     *
     * Initializes the timeout to the specified value in milliseconds.
     *
     * @param timeout milliseconds to pass to Socket's connect() method.
     */
    public TimeoutRMISocketFactory(final int timeout) {
        super();

        this.timeout = timeout;
    }

    /**
     * <p>Setter for timeout.</p>
     *
     * Resets the timeout to the specified milliseconds value.
     *
     * @param timeout milliseconds to pass to Socket's connect() method.
     *
     * @return Modified TimeoutRMISocketFactory for setter chaining.
     */
    public final TimeoutRMISocketFactory setTimeout(final int timeout) {
        this.timeout = timeout;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final Socket createSocket(final String host, final int port) throws IOException {
        LOG.debug("creating new socket with " + timeout + "ms timeout");

        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), timeout);

        return socket;
    }

    /** {@inheritDoc} */
    @Override
    public final ServerSocket createServerSocket(final int port) throws IOException {
        return new ServerSocket(port);
    }
}
