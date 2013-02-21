package com.topsy.jmxproxy.resource;

import com.topsy.jmxproxy.service.JMXConnectionManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import org.springframework.stereotype.Service;

import com.sun.jersey.api.core.InjectParam;

@Service
@Path("/")
public class JMXProxyResource {
    private static Logger LOG = Logger.getLogger(JMXProxyResource.class);

    @InjectParam
    private static JMXConnectionManager manager;

    @GET
    @Path("/{host}:{port:\\d+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJMXDataJSON(@PathParam("host") String host, @PathParam("port") int port) {
        LOG.debug("request jmx domains as json for " + host + ":" + port);

        try {
            return Response.ok(manager.getHost(host + ":" + port)).build();
        } catch (Exception e) {
            LOG.debug("failed parameters: " + host + ":" + port, e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
