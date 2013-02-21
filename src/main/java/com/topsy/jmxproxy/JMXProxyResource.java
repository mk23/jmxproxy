package com.topsy.jmxproxy;

import com.topsy.jmxproxy.core.Host;
import com.topsy.jmxproxy.jmx.ConnectionManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

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

    public JMXProxyResource(ConnectionManager manager) {
        this.manager = manager;
    }

    @GET
    public Host getJMXHostData(@PathParam("host") String host, @PathParam("port") int port) {
        LOG.debug("fetching jmx data for " + host + ":" + port);

        try {
            return manager.getHost(host + ":" + port);
        } catch (Exception e) {
            LOG.debug("failed parameters: " + host + ":" + port, e);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }
}
