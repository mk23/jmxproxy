package com.github.mk23.jmxproxy;

import com.github.mk23.jmxproxy.core.Host;
import com.github.mk23.jmxproxy.core.MBean;
import com.github.mk23.jmxproxy.jmx.ConnectionCredentials;
import com.github.mk23.jmxproxy.jmx.ConnectionManager;

import io.dropwizard.jersey.params.BooleanParam;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Service API resource handlers.</p>
 *
 * Provides handler methods for the service resources.  Maintains the
 * lifecycle {@link ConnectionManager} object for fulfilling requests.
 * All responses are marshalled to <code>application/json</code>
 * content type by Jersey.
 *
 * @see <a href="http://docs.oracle.com/javaee/7/api/javax/ws/rs/core/Response.html">javax.ws.rs.core.Response</a>
 *
 * @since   2015-05-11
 * @author  mk23
 * @version 3.2.0
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class JMXProxyResource {
    private static final Logger LOG = LoggerFactory.getLogger(JMXProxyResource.class);

    /**
     * Lifecycle {@link ConnectionManager} object that provides
     * responses from its maintained endpoint cache.
     */
    private final ConnectionManager manager;

    /**
     * <p>Default constructor.</p>
     *
     * Saves the {@link ConnectionManager} object for querying on
     * API requests.
     *
     * @param manager of the endpoint cache.
     */
    public JMXProxyResource(final ConnectionManager manager) {
        this.manager = manager;
    }

    /**
     * <p>Anonymous GET handler for <code>/jmxproxy/config</code>.</p>
     *
     * Accepts HTTP GET requests.  Responds with the full runtime configuration
     * object from the {@link ConnectionManager}.
     *
     * @return The runtime configuration object marshalled into an API Response.
     */
    @GET
    @Path("config")
    public final Response getConfiguration() {
        return Response.ok(manager.getConfiguration()).build();
    }

    /**
     * <p>Anonymous GET handler for <code>/&lt;host&gt;:&lt;port&gt;/&lt;mbean&gt;/&lt;attribute&gt;</code>.</p>
     *
     * Accepts HTTP GET requests.  Responds with a single value or a list of
     * historical values for the requested attribute.
     *
     * @param host requested endpoint DNS host name or IP address.
     *     Specified on the URI path.
     * @param port requested endpoint TCP port.
     *     Specified on the URI path.
     * @param mbean name of the mbean on the requested endpoint.
     *     Specified on the URI path.
     * @param attribute name of the attribute on the requested mbean
     *     Specified on the URI path.
     * @param limit number of historical values to include when fetching the full map.
     *     If set to -1 (default) will only return latest value.
     *     Specified as a URI query param.
     *
     * @return The requested value, or list of values, marshalled into an API Response.
     */
    @GET
    @Path("{host}:{port:\\d+}/{mbean}/{attribute}")
    public final Response getJMXHostData(
        @PathParam("host") final String host,
        @PathParam("port") final int port,
        @PathParam("mbean") final String mbean,
        @PathParam("attribute") final String attribute,
        @QueryParam("limit") @DefaultValue("-1") final int limit
    ) {
        LOG.debug(String.format("fetching %s:%d/%s/%s", host, port, mbean, attribute));
        return getJMXHost(host, port, mbean, attribute, limit, null);
    }

    /**
     * <p>Authenticated POST handler for <code>/&lt;host&gt;:&lt;port&gt;/&lt;mbean&gt;/&lt;attribute&gt;</code>.</p>
     *
     * Accepts HTTP POST requests with data encoded as
     * <code>application/x-www-form-urlencoded</code>, containing endpoint
     * credentials.  Responds with a single value or a list of historical
     * values for the requested attribute.
     *
     * @param host requested endpoint DNS host name or IP address.
     *     Specified on the URI path.
     * @param port requested endpoint TCP port.
     *     Specified on the URI path.
     * @param mbean name of the mbean on the requested endpoint.
     *     Specified on the URI path.
     * @param attribute name of the attribute on the requested mbean
     *     Specified on the URI path.
     * @param limit number of historical values to include when fetching the full map.
     *     If set to -1 (default) will only return latest value.
     *     Specified as a URI query param.
     * @param username authentication username for the requested endpoint.
     *     Specified as the POST body param.
     * @param password authentication password for the requested endpoint.
     *     Specified as the POST body param.
     *
     * @return The requested value, or list of values, marshalled into an API Response.
     */
    @POST
    @Path("{host}:{port:\\d+}/{mbean}/{attribute}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public final Response getJMXHostData(
        @PathParam("host") final String host,
        @PathParam("port") final int port,
        @PathParam("mbean") final String mbean,
        @PathParam("attribute") final String attribute,
        @QueryParam("limit") @DefaultValue("-1") final int limit,
        @FormParam("username") final String username,
        @FormParam("password") final String password
    ) {
        LOG.debug(String.format("fetching %s@%s:%d/%s/%s", username, host, port, mbean, attribute));
        return getJMXHost(host, port, mbean, attribute, limit, new ConnectionCredentials(username, password));
    }

    /**
     * <p>Authenticated POST handler for <code>/&lt;host&gt;:&lt;port&gt;/&lt;mbean&gt;/&lt;attribute&gt;</code>.</p>
     *
     * Accepts HTTP POST requests with data encoded as
     * <code>application/json</code>, containing endpoint credentials.
     * Responds with a single value or a list of historical values for
     * the requested attribute.
     *
     * @param host requested endpoint DNS host name or IP address.
     *     Specified on the URI path.
     * @param port requested endpoint TCP port.
     *     Specified on the URI path.
     * @param mbean name of the mbean on the requested endpoint.
     *     Specified on the URI path.
     * @param attribute name of the attribute on the requested mbean
     *     Specified on the URI path.
     * @param limit number of historical values to include when fetching the full map.
     *     If set to -1 (default) will only return latest value.
     *     Specified as a URI query param.
     * @param auth credentials for the requested endpoint specified as the POST body
     *     param as a JSON object (e.g. <code>{"username": "max", "password": "12345"}</code>)
     *     and marshaled into {@link ConnectionCredentials}.
     *
     * @return The requested value, or list of values, marshalled into an API Response.
     */
    @POST
    @Path("{host}:{port:\\d+}/{mbean}/{attribute}")
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response getJMXHostData(
        @PathParam("host") final String host,
        @PathParam("port") final int port,
        @PathParam("mbean") final String mbean,
        @PathParam("attribute") final String attribute,
        @QueryParam("limit") @DefaultValue("-1") final int limit,
        @Valid final ConnectionCredentials auth
    ) {
        LOG.debug(String.format("fetching %s@%s:%d/%s/%s", auth.getUsername(), host, port, mbean, attribute));
        return getJMXHost(host, port, mbean, attribute, limit, auth);
    }

    /**
     * <p>Anonymous GET handler for <code>/&lt;host&gt;:&lt;port&gt;/&lt;mbean&gt;</code>.</p>
     *
     * Accepts HTTP GET requests.  Responds with a list of attributes
     * available for the requested mbean or a map of attribute name and
     * value(s) based on modifier parameters.
     *
     * @param host requested endpoint DNS host name or IP address.
     *     Specified on the URI path.
     * @param port requested endpoint TCP port.
     *     Specified on the URI path.
     * @param mbean name of the mbean on the requested endpoint.
     *     Specified on the URI path.
     * @param limit number of historical values to include when fetching the full map.
     *     If set to -1 (default) will only return latest value.
     *     Specified as a URI query param.
     * @param full boolean controlling whether to return full map of names and values or
     *     just a list of attribute names.
     *     Specified as a URI query param.
     *
     * @return List of attribute names or map of names and values, marshalled
     *     into an API Response.
     */
    @GET
    @Path("{host}:{port:\\d+}/{mbean}")
    public final Response getJMXHostData(
        @PathParam("host") final String host,
        @PathParam("port") final int port,
        @PathParam("mbean") final String mbean,
        @QueryParam("limit") @DefaultValue("-1") final int limit,
        @QueryParam("full") @DefaultValue("false") final BooleanParam full
    ) {
        LOG.debug(String.format("fetching %s:%d/%s (full:%s)", host, port, mbean, full.get()));
        return getJMXHost(host, port, mbean, full.get(), limit, null);
    }

    /**
     * <p>Authenticated POST handler for <code>/&lt;host&gt;:&lt;port&gt;/&lt;mbean&gt;</code>.</p>
     *
     * Accepts HTTP POST requests with data encoded as
     * <code>application/x-www-form-urlencoded</code>, containing endpoint
     * credentials.  Responds with a list of attributes available for the
     * requested mbean or a map of attribute name and value(s) based on
     * modifier parameters.
     *
     * @param host requested endpoint DNS host name or IP address.
     *     Specified on the URI path.
     * @param port requested endpoint TCP port.
     *     Specified on the URI path.
     * @param mbean name of the mbean on the requested endpoint.
     *     Specified on the URI path.
     * @param limit number of historical values to include when fetching the full map.
     *     If set to -1 (default) will only return latest value.
     *     Specified as a URI query param.
     * @param full boolean controlling whether to return full map of names and values or
     *     just a list of attribute names.
     *     Specified as a URI query param.
     * @param username authentication username for the requested endpoint.
     *     Specified as the POST body param.
     * @param password authentication password for the requested endpoint.
     *     Specified as the POST body param.
     *
     * @return List of attribute names or map of names and values, marshalled
     *     into an API Response.
     */
    @POST
    @Path("{host}:{port:\\d+}/{mbean}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public final Response getJMXHostData(
        @PathParam("host") final String host,
        @PathParam("port") final int port,
        @PathParam("mbean") final String mbean,
        @QueryParam("limit") @DefaultValue("-1") final int limit,
        @QueryParam("full") @DefaultValue("false") final BooleanParam full,
        @FormParam("username") final String username,
        @FormParam("password") final String password
    ) {
        LOG.debug(String.format("fetching %s@%s:%d/%s (full:%s)", username, host, port, mbean, full.get()));
        return getJMXHost(host, port, mbean, full.get(), limit, new ConnectionCredentials(username, password));
    }

    /**
     * <p>Authenticated POST handler for <code>/&lt;host&gt;:&lt;port&gt;/&lt;mbean&gt;</code>.</p>
     *
     * Accepts HTTP POST requests with data encoded as
     * <code>application/json</code>, containing endpoint credentials.
     * Responds with a list of attributes available for the requested
     * mbean or a map of attribute name and value(s) based on modifier
     * parameters.
     *
     * @param host requested endpoint DNS host name or IP address.
     *     Specified on the URI path.
     * @param port requested endpoint TCP port.
     *     Specified on the URI path.
     * @param mbean name of the mbean on the requested endpoint.
     *     Specified on the URI path.
     * @param limit number of historical values to include when fetching the full map.
     *     If set to -1 (default) will only return latest value.
     *     Specified as a URI query param.
     * @param full boolean controlling whether to return full map of names and values or
     *     just a list of attribute names.
     *     Specified as a URI query param.
     * @param auth credentials for the requested endpoint specified as the POST body
     *     param as a JSON object (e.g. <code>{"username": "max", "password": "12345"}</code>)
     *     and marshaled into {@link ConnectionCredentials}.
     *
     * @return List of attribute names or map of names and values, marshalled
     *     into an API Response.
     */
    @POST
    @Path("{host}:{port:\\d+}/{mbean}")
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response getJMXHostData(
        @PathParam("host") final String host,
        @PathParam("port") final int port,
        @PathParam("mbean") final String mbean,
        @QueryParam("limit") @DefaultValue("-1") final int limit,
        @QueryParam("full") @DefaultValue("false") final BooleanParam full,
        @Valid final ConnectionCredentials auth
    ) {
        LOG.debug(String.format("fetching %s@%s:%d/%s (full:%s)", auth.getUsername(), host, port, mbean, full.get()));
        return getJMXHost(host, port, mbean, full.get(), limit, auth);
    }

    /**
     * <p>Anonymous GET handler for <code>/&lt;host&gt;:&lt;port&gt;</code>.</p>
     *
     * Accepts HTTP GET requests.  Responds with a list of mbeans
     * available on the requested endpoint or a map of means, attributes
     * and their values or lists of values based on modifier parameters.
     *
     * @param host requested endpoint DNS host name or IP address.
     *     Specified on the URI path.
     * @param port requested endpoint TCP port.
     *     Specified on the URI path.
     * @param limit number of historical values to include when fetching the full map.
     *     If set to -1 (default) will only return latest value.
     *     Specified as a URI query param.
     * @param full boolean controlling whether to return full map of names and values or
     *     just a list of attribute names.
     *     Specified as a URI query param.
     *
     * @return List of mbean names or map of names and values, marshalled
     *     into an API Response.
     */
    @GET
    @Path("{host}:{port:\\d+}")
    public final Response getJMXHostData(
        @PathParam("host") final String host,
        @PathParam("port") final int port,
        @QueryParam("limit") @DefaultValue("-1") final int limit,
        @QueryParam("full") @DefaultValue("false") final BooleanParam full
    ) {
        LOG.debug(String.format("fetching %s:%d (full:%s)", host, port, full.get()));
        return getJMXHost(host, port, full.get(), limit, null);
    }

    /**
     * <p>Authenticated POST handler for <code>/&lt;host&gt;:&lt;port&gt;</code>.</p>
     *
     * Accepts HTTP POST requests with data encoded as
     * <code>application/x-www-form-urlencoded</code>, containing endpoint
     * credentials.  Responds with a list of mbeans available on the requested
     * endpoint or a map of means, attributes and their values or lists of
     * values based on modifier parameters.
     *
     * @param host requested endpoint DNS host name or IP address.
     *     Specified on the URI path.
     * @param port requested endpoint TCP port.
     *     Specified on the URI path.
     * @param limit number of historical values to include when fetching the full map.
     *     If set to -1 (default) will only return latest value.
     *     Specified as a URI query param.
     * @param full boolean controlling whether to return full map of names and values or
     *     just a list of attribute names.
     *     Specified as a URI query param.
     * @param username authentication username for the requested endpoint.
     *     Specified as the POST body param.
     * @param password authentication password for the requested endpoint.
     *     Specified as the POST body param.
     *
     * @return List of mbean names or map of names and values, marshalled
     *     into an API Response.
     */
    @POST
    @Path("{host}:{port:\\d+}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public final Response getJMXHostData(
        @PathParam("host") final String host,
        @PathParam("port") final int port,
        @QueryParam("limit") @DefaultValue("-1") final int limit,
        @QueryParam("full") @DefaultValue("false") final BooleanParam full,
        @FormParam("username") final String username,
        @FormParam("password") final String password
    ) {
        LOG.debug(String.format("fetching %s@%s:%d (full:%s)", username, host, port, full.get()));
        return getJMXHost(host, port, full.get(), limit, new ConnectionCredentials(username, password));
    }

    /**
     * <p>Authenticated POST handler for <code>/&lt;host&gt;:&lt;port&gt;</code>.</p>
     *
     * Accepts HTTP POST requests with data encoded as
     * <code>application/json</code>, containing endpoint credentials.
     * Responds with a list of mbeans available on the requested endpoint
     * or a map of means, attributes and their values or lists of values
     * based on modifier parameters.
     *
     * @param host requested endpoint DNS host name or IP address.
     *     Specified on the URI path.
     * @param port requested endpoint TCP port.
     *     Specified on the URI path.
     * @param limit number of historical values to include when fetching the full map.
     *     If set to -1 (default) will only return latest value.
     *     Specified as a URI query param.
     * @param full boolean controlling whether to return full map of names and values or
     *     just a list of attribute names.
     *     Specified as a URI query param.
     * @param auth credentials for the requested endpoint specified as the POST body
     *     param as a JSON object (e.g. <code>{"username": "max", "password": "12345"}</code>)
     *     and marshaled into {@link ConnectionCredentials}.
     *
     * @return List of mbean names or map of names and values, marshalled
     *     into an API Response.
     */
    @POST
    @Path("{host}:{port:\\d+}")
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response getJMXHostData(
        @PathParam("host") final String host,
        @PathParam("port") final int port,
        @QueryParam("limit") @DefaultValue("-1") final int limit,
        @QueryParam("full") @DefaultValue("false") final BooleanParam full,
        @Valid final ConnectionCredentials auth
    ) {
        LOG.debug(String.format("fetching %s@%s:%d (full:%s)", auth.getUsername(), host, port, full.get()));
        return getJMXHost(host, port, full.get(), limit, auth);
    }

    private Response getJMXHost(
        final String hostName,
        final int port,
        final boolean full,
        final int limit,
        final ConnectionCredentials auth
    ) {
        Host host = manager.getHost(hostName + ":" + port, auth);
        if (host == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(full ? host.setLimit(limit) : host.getMBeans()).build();
    }

    private Response getJMXHost(
        final String hostName,
        final int port,
        final String mbeanName,
        final boolean full,
        final int limit,
        final ConnectionCredentials auth
    ) {
        Host host = manager.getHost(hostName + ":" + port, auth);
        if (host == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        MBean mbean = host.getMBean(mbeanName);
        if (mbean == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(full ? mbean.setLimit(limit) : mbean.getAttributes()).build();
    }

    private Response getJMXHost(
        final String hostName,
        final int port,
        final String mbeanName,
        final String attribute,
        final int limit,
        final ConnectionCredentials auth
    ) {
        Host host = manager.getHost(hostName + ":" + port, auth);
        if (host == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        MBean mbean = host.getMBean(mbeanName);
        if (mbean == null || !mbean.hasAttribute(attribute)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (limit < 0) {
            return Response.ok(mbean.getAttribute(attribute)).build();
        } else {
            return Response.ok(mbean.getAttributes(attribute, limit)).build();
        }
    }
}
