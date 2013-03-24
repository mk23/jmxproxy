package com.topsy.jmxproxy;

import com.topsy.jmxproxy.core.Host;
import com.topsy.jmxproxy.jmx.ConnectionManager;

import com.yammer.dropwizard.jersey.params.BooleanParam;

import javax.ws.rs.DefaultValue;
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

    public JMXProxyResource(ConnectionManager manager) {
        this.manager = manager;
    }

    @GET
    public Host getJMXHostData(@PathParam("host") String hostName, @PathParam("port") int port, @QueryParam("domains") @DefaultValue("false") domains) {
        LOG.debug("fetching jmx data for " + hostName + ":" + port + " (domains:" + domains.get() + ")");

        try {
            Host host = manager.getHost(hostName + ":" + port);
            host.toggleDomains(domains.get());
            return host;
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }
}
