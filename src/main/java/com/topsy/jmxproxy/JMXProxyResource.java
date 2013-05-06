package com.topsy.jmxproxy;

import com.topsy.jmxproxy.core.Host;
import com.topsy.jmxproxy.jmx.ConnectionCredentials;
import com.topsy.jmxproxy.jmx.ConnectionManager;

import com.yammer.dropwizard.jersey.params.BooleanParam;

import javax.security.auth.login.FailedLoginException;

import javax.validation.Valid;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
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
    public Host getJMXHostData(@PathParam("host") String host, @PathParam("port") int port, @QueryParam("full") @DefaultValue("true") BooleanParam full) {
        LOG.debug("fetching jmx data for " + host + ":" + port + " (full:" + full.get() + ")");
        return getJMXHost(host, port, full.get(), null);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Host getJMXHostData(@PathParam("host") String host, @PathParam("port") int port, @FormParam("username") String username, @FormParam("password") String password, @QueryParam("full") @DefaultValue("false") BooleanParam full) {
        LOG.debug("fetching jmx data for " + username + "@" + host + ":" + port + " (full:" + full.get() + ")");
        return getJMXHost(host, port, full.get(), new ConnectionCredentials(username, password));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Host getJMXHostData(@PathParam("host") String host, @PathParam("port") int port, @QueryParam("full") @DefaultValue("false") BooleanParam full, @Valid ConnectionCredentials auth) {
        LOG.debug("fetching jmx data for " + auth.getUsername() + "@" + host + ":" + port + " (full:" + full.get() + ")");

        return getJMXHost(host, port, full.get(), auth);
    }

    private Host getJMXHost(String hostName, int port, boolean full, ConnectionCredentials auth) {
        try {
            Host host = manager.getHost(hostName + ":" + port, auth);
            if (full) {
                return host;
            }
            return null; // temporary
        } catch (java.lang.SecurityException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }
}
