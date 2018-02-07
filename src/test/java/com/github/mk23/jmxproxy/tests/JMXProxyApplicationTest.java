package com.github.mk23.jmxproxy.tests;

import com.github.mk23.jmxproxy.JMXProxyApplication;
import com.github.mk23.jmxproxy.conf.MainConfig;
import com.github.mk23.jmxproxy.jmx.ConnectionManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.FixtureHelpers;
import io.dropwizard.testing.ResourceHelpers;

import java.io.FileNotFoundException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.util.component.LifeCycle;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JMXProxyApplicationTest {

    private static final String CONFIG =
        ResourceHelpers.resourceFilePath("main_config.yaml");
    public static final DropwizardTestSupport<MainConfig> SUPPORT =
        new DropwizardTestSupport<MainConfig>(JMXProxyApplication.class, CONFIG);

    private Client client;

    @Rule public ExpectedException thrown = ExpectedException.none();
    @Rule public TestName name = new TestName();

    @Before
    public void printTestName() {
        System.out.println(" -> " + name.getMethodName());
    }

    @Before
    public void startApplication() throws Exception {
        SUPPORT.before();
    }

    @Before
    public void createClient() throws Exception {
        client = ClientBuilder.newClient();
    }

    @After
    public void stopApplication() throws Exception {
        SUPPORT.after();
    }

    @After
    public void shutdownClient() throws Exception {
        client.close();
    }

    @Test
    public void checkApplication() throws Exception {
        Response response = client.target("http://localhost:" + SUPPORT.getLocalPort()).request().get();

        assertTrue(response.getStatus() == Response.Status.OK.getStatusCode());
        assertTrue(response.readEntity(String.class).contains("<title>JMXProxy</title>"));
    }

    @Test
    public void checkMinificationJS() throws Exception {
        Response response = client.target("http://localhost:" + SUPPORT.getLocalPort() + "/js/jmxproxy.js").request().get();

        assertTrue(response.getStatus() == Response.Status.OK.getStatusCode());
        assertFalse(response.readEntity(String.class).contains("var endpointHost;"));
    }

    @Test
    public void checkMinificationCSS() throws Exception {
        Response response = client.target("http://localhost:" + SUPPORT.getLocalPort() + "/css/jmxproxy.css").request().get();

        assertTrue(response.getStatus() == Response.Status.OK.getStatusCode());
        assertFalse(response.readEntity(String.class).contains("\n"));
    }

    @Test
    public void checkHealthStatusPass() throws Exception {
        ObjectMapper om = new ObjectMapper();
        String expected = om.writeValueAsString(om.readValue(FixtureHelpers.fixture("fixtures/health_check_pass.json"), JsonNode.class));

        Response response = client.target("http://localhost:" + SUPPORT.getAdminPort() + "/healthcheck").request().get();

        assertTrue(response.getStatus() == Response.Status.OK.getStatusCode());
        assertTrue(response.readEntity(String.class).equals(expected));
    }

    @Test
    public void checkHealthStatusFail() throws Exception {
        ObjectMapper om = new ObjectMapper();
        String expected = om.writeValueAsString(om.readValue(FixtureHelpers.fixture("fixtures/health_check_fail.json"), JsonNode.class));

        LifeCycle lc = null;
        for (LifeCycle object : SUPPORT.getEnvironment().lifecycle().getManagedObjects()) {
            if (object.toString().startsWith(ConnectionManager.class.getName())) {
                lc = object;
                break;
            }
        }
        assertNotNull(lc);
        lc.stop();

        Response response = client.target("http://localhost:" + SUPPORT.getAdminPort() + "/healthcheck").request().get();

        assertTrue(response.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertTrue(response.readEntity(String.class).equals(expected));
    }

    @Test
    public void checkMainValidConfig() throws Exception {
        JMXProxyApplication.main(new String[] {"server", CONFIG});
        // success if no exceptions are thrown
    }
}
