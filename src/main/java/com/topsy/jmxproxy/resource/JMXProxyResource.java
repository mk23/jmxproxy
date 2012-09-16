package com.topsy.jmxproxy.resource;

import com.topsy.jmxproxy.service.JMXConnectionManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import org.springframework.stereotype.Service;

import com.sun.jersey.spi.inject.Inject;

@Service
@Path("/")
public class JMXProxyResource {
    private static Logger LOG = Logger.getLogger(JMXProxyResource.class);

    @Inject
    private static JMXConnectionManager manager;

    @GET
    @Path("/{host}.json")
    @Produces("application/json")
    public Response getJMXDataJSON(@PathParam("host") String host) {
        LOG.debug("request jmx domains as json for " + host);

        try {
            return Response.ok(manager.getHost(host)).build();
        } catch (Exception e) {
            LOG.debug("failed parameters: " + host, e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/{host}.xml")
    @Produces("application/xml")
    public Response getJMXDataXML(@PathParam("host") String host) {
        LOG.debug("request jmx domains as xml for " + host);

        try {
            return Response.ok(manager.getHost(host)).build();
        } catch (Exception e) {
            LOG.debug("failed parameters: " + host, e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
