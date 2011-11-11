package com.topsy.jmxproxy.resource;

import java.io.UnsupportedEncodingException;

import java.lang.StringBuilder;

import java.net.URLEncoder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import com.sun.jersey.spi.inject.Inject;

import com.topsy.jmxproxy.service.JMXConnectionManager;
import com.topsy.jmxproxy.domain.Attribute;

@Service
@Path("/")
public class JMXProxyResource {
    private static Logger logger = Logger.getLogger(JMXProxyResource.class);

    @Inject
    private static JMXConnectionManager manager;

    private String encode(Object item) {
        try {
            return URLEncoder.encode(item.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return item.toString();
        }
    }

    private String escape(Object item) {
        return HtmlUtils.htmlEscape(item.toString());
    }

    @GET
    @Path("/{host}")
    public Response processJMXRequest(@PathParam("host") String host) {
        logger.debug("requested jmx domains from " + host);
        try {
            StringBuilder sb = new StringBuilder("<html><head><title>" + escape(host) + "</title></head><body><ul>");
            for (String domain : manager.getDomains(host)) {
                sb.append("\n<li><a href='" + encode(host) + "/" + encode(domain) + "'>" + escape(domain) + "</a></li>");
            }
            sb.append("\n</ul></body></html>");
            return Response.ok(sb.toString()).build();
        } catch (Exception e) {
            logger.debug(e.fillInStackTrace());
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/{host}/{object}")
    public Response processJMXRequest(@PathParam("host") String host, @PathParam("object") String object) {
        try {
            StringBuilder sb = new StringBuilder("<html><head><title>" + escape(host) + "</title></head><body><ul>");
            if (object.indexOf(':') == -1) {
                logger.debug("requested jmx domain mbeans from " + host + "/" + object);
                for (String mbean : manager.getMBeans(host, object)) {
                    sb.append("\n<li><a href='" + encode(mbean) + "'>" + escape(mbean) + "</a></li>");
                }
            } else {
                logger.debug("requested jmx mbean attributes from " + host + "/" + object);
                for (String attribute : manager.getAttributes(host, object)) {
                    sb.append("\n<li><a href='" + encode(object) + "/" + encode(attribute) + "'>" + escape(attribute) + "</a></li>");
                }
            }
            sb.append("\n</ul></body></html>");
            return Response.ok(sb.toString()).build();
        } catch (Exception e) {
            logger.debug(e.fillInStackTrace());
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Produces("application/json")
    @Path("/{host}/{mbean}/{attribute}")
    public Response processJMXRequest(@PathParam("host") String host, @PathParam("mbean") String mbean, @PathParam("attribute") String attribute) {
        logger.debug("requested jmx attribute value of " + host + "/" + mbean + "/" + attribute);
        try {
            Object value = manager.getAttributeValue(host, mbean, attribute);
            return Response.ok(Attribute.toJSONString(value)).build();
        } catch (Exception e) {
            logger.debug(e.fillInStackTrace());
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
