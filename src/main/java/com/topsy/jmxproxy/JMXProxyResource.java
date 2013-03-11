package com.topsy.jmxproxy;

import com.google.common.base.Optional;

import com.topsy.jmxproxy.core.Host;
import com.topsy.jmxproxy.jmx.ConnectionManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/{host}:{port:\\d+}")
@Produces(MediaType.APPLICATION_JSON)
public class JMXProxyResource {
    private static final Logger LOG = LoggerFactory.getLogger(JMXProxyResource.class);

    private final ConnectionManager manager;
    private final JMXProxyServiceConfiguration config;

    public JMXProxyResource(ConnectionManager manager, JMXProxyServiceConfiguration config) {
        this.manager = manager;
        this.config = config;
    }

    @GET
    public Host getJMXHostData(@PathParam("host") String hostName, @PathParam("port") int port, @QueryParam("domains") Optional<Boolean> includeDomains) {
        LOG.debug("fetching jmx data for " + hostName + ":" + port + " (domains:" + includeDomains.or(false) + ")");

        try {
            Host host = manager.getHost(hostName + ":" + port);
            host.toggleDomains(includeDomains.or(false));
            return host;
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }
}
