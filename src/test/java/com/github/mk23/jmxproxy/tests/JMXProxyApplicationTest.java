package com.github.mk23.jmxproxy.tests;

import com.github.mk23.jmxproxy.JMXProxyApplication;
import com.github.mk23.jmxproxy.conf.MainConfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.testing.FixtureHelpers;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JMXProxyApplicationTest {

    private static final String CONFIG = ResourceHelpers.resourceFilePath("main_config.yaml");
    private Client client;

    @Rule public TestName name = new TestName();

    @ClassRule
    public static final DropwizardAppRule<MainConfig> RULE =
        new DropwizardAppRule<MainConfig>(JMXProxyApplication.class, CONFIG);

    @Before
    public void printTestName() {
        System.out.println(" -> " + name.getMethodName());
    }

    @Before
    public void createClient() throws Exception {
        client = ClientBuilder.newClient();
    }

    @After
    public void shutdownClient() throws Exception {
        client.close();
    }

    @Test
    public void checkApplication() throws Exception {
        String acquired = client.target("http://localhost:" + RULE.getLocalPort())
            .request()
            .get(String.class);

        assertTrue(acquired.contains("<title>JMXProxy</title>"));
    }

    @Test
    public void checkMinificationJS() throws Exception {
        String acquired = client.target("http://localhost:" + RULE.getLocalPort() + "/js/jmxproxy.js")
            .request()
            .get(String.class);

        assertFalse(acquired.contains("\n"));
    }

    @Test
    public void checkMinificationCSS() throws Exception {
        String acquired = client.target("http://localhost:" + RULE.getLocalPort() + "/css/jmxproxy.css")
            .request()
            .get(String.class);

        assertFalse(acquired.contains("\n"));
    }

    @Test
    public void checkHealthStatus() throws Exception {
        String acquired = client.target("http://localhost:" + RULE.getAdminPort() + "/healthcheck")
            .request()
            .get(String.class);

        ObjectMapper om = new ObjectMapper();
        String expected = om.writeValueAsString(om.readValue(FixtureHelpers.fixture("fixtures/health_check.json"), JsonNode.class));

        assertTrue(expected.equals(acquired));
    }
}
