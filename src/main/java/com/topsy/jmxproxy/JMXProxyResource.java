package com.topsy.jmxproxy;

import com.topsy.jmxproxy.core.Host;
import com.topsy.jmxproxy.core.MBean;
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

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class JMXProxyResource {
    private static final Logger LOG = LoggerFactory.getLogger(JMXProxyResource.class);

    private final ConnectionManager manager;

    public JMXProxyResource(ConnectionManager manager) {
        this.manager = manager;
    }

    @GET
    @Path("{host}:{port:\\d+}/{mbean}/{attribute}")
    public Response getJMXHostData(@PathParam("host") String host, @PathParam("port") int port, @PathParam("mbean") String mbean, @PathParam("attribute") String attribute) {
        LOG.debug("fetching jmx data for " + host + ":" + port + "/" + mbean + "/" + attribute);
        return getJMXHost(host, port, mbean, attribute, null);
    }

    @POST
    @Path("{host}:{port:\\d+}/{mbean}/{attribute}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getJMXHostData(@PathParam("host") String host, @PathParam("port") int port, @PathParam("mbean") String mbean, @PathParam("attribute") String attribute, @FormParam("username") String username, @FormParam("password") String password) {
        LOG.debug("fetching jmx data for " + username + "@" + host + ":" + port + "/" + mbean + "/" + attribute);
        return getJMXHost(host, port, mbean, attribute, new ConnectionCredentials(username, password));
    }

    @POST
    @Path("{host}:{port:\\d+}/{mbean}/{attribute}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getJMXHostData(@PathParam("host") String host, @PathParam("port") int port, @PathParam("mbean") String mbean, @PathParam("attribute") String attribute, @Valid ConnectionCredentials auth) {
        LOG.debug("fetching jmx data for " + auth.getUsername() + "@" + host + ":" + port + "/" + mbean + "/" + attribute);

        return getJMXHost(host, port, mbean, attribute, auth);
    }

    @GET
    @Path("{host}:{port:\\d+}/{mbean}")
    public Response getJMXHostData(@PathParam("host") String host, @PathParam("port") int port, @PathParam("mbean") String mbean, @QueryParam("full") @DefaultValue("false") BooleanParam full) {
        LOG.debug("fetching jmx data for " + host + ":" + port + "/" + mbean + " (full:" + full.get() + ")");
        return getJMXHost(host, port, mbean, full.get(), null);
    }

    @POST
    @Path("{host}:{port:\\d+}/{mbean}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getJMXHostData(@PathParam("host") String host, @PathParam("port") int port, @PathParam("mbean") String mbean, @FormParam("username") String username, @FormParam("password") String password, @QueryParam("full") @DefaultValue("false") BooleanParam full) {
        LOG.debug("fetching jmx data for " + username + "@" + host + ":" + port + "/" + mbean + " (full:" + full.get() + ")");
        return getJMXHost(host, port, mbean, full.get(), new ConnectionCredentials(username, password));
    }

    @POST
    @Path("{host}:{port:\\d+}/{mbean}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getJMXHostData(@PathParam("host") String host, @PathParam("port") int port, @PathParam("mbean") String mbean, @QueryParam("full") @DefaultValue("false") BooleanParam full, @Valid ConnectionCredentials auth) {
        LOG.debug("fetching jmx data for " + auth.getUsername() + "@" + host + ":" + port + "/" + mbean + " (full:" + full.get() + ")");

        return getJMXHost(host, port, mbean, full.get(), auth);
    }

    @GET
    @Path("{host}:{port:\\d+}")
    public Response getJMXHostData(@PathParam("host") String host, @PathParam("port") int port, @QueryParam("full") @DefaultValue("false") BooleanParam full) {
        LOG.debug("fetching jmx data for " + host + ":" + port + " (full:" + full.get() + ")");
        return getJMXHost(host, port, full.get(), null);
    }

    @POST
    @Path("{host}:{port:\\d+}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getJMXHostData(@PathParam("host") String host, @PathParam("port") int port, @FormParam("username") String username, @FormParam("password") String password, @QueryParam("full") @DefaultValue("false") BooleanParam full) {
        LOG.debug("fetching jmx data for " + username + "@" + host + ":" + port + " (full:" + full.get() + ")");
        return getJMXHost(host, port, full.get(), new ConnectionCredentials(username, password));
    }

    @POST
    @Path("{host}:{port:\\d+}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getJMXHostData(@PathParam("host") String host, @PathParam("port") int port, @QueryParam("full") @DefaultValue("false") BooleanParam full, @Valid ConnectionCredentials auth) {
        LOG.debug("fetching jmx data for " + auth.getUsername() + "@" + host + ":" + port + " (full:" + full.get() + ")");

        return getJMXHost(host, port, full.get(), auth);
    }

    private Response getJMXHost(String hostName, int port, boolean full, ConnectionCredentials auth) {
        try {
            Host host = manager.getHost(hostName + ":" + port, auth);
            if (host == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            return Response.ok(full ? host : host.getMBeans()).build();
        } catch (java.lang.SecurityException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    private Response getJMXHost(String hostName, int port, String mbeanName, boolean full, ConnectionCredentials auth) {
        try {
            Host host = manager.getHost(hostName + ":" + port, auth);
            if (host == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            MBean mbean = host.getMBean(mbeanName);
            if (mbean == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            return Response.ok(full ? mbean : mbean.getAttributes()).build();
        } catch (java.lang.SecurityException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    private Response getJMXHost(String hostName, int port, String mbeanName, String attribute, ConnectionCredentials auth) {
        try {
            Host host = manager.getHost(hostName + ":" + port, auth);
            if (host == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            MBean mbean = host.getMBean(mbeanName);
            if (mbean == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            return Response.ok(mbean.getAttribute(attribute)).build();
        } catch (java.lang.SecurityException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }
}
