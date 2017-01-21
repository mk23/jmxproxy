package com.github.mk23.jmxproxy.tests;

import com.github.mk23.jmxproxy.JMXProxyResource;
import com.github.mk23.jmxproxy.conf.AppConfig;
import com.github.mk23.jmxproxy.jmx.ConnectionCredentials;
import com.github.mk23.jmxproxy.jmx.ConnectionManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.testing.FixtureHelpers;
import io.dropwizard.testing.junit.ResourceTestRule;
import io.dropwizard.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.junit.Assume.assumeNotNull;

public class JMXProxyResourceTest {
    private final String passwdFile       = System.getProperty("com.sun.management.jmxremote.password.file");

    private final String validHost        = "localhost:" + System.getProperty("com.sun.management.jmxremote.port");
    private final String invalidHost      = "localhost:0";

    private final String validMBean       = "java.lang:type=OperatingSystem";
    private final String invalidMBean     = "java.lang:type=InvalidMBean";

    private final String validAttribute   = "AvailableProcessors";
    private final String invalidAttribute = "InvalidAttribute";

    private final int validValue          = Runtime.getRuntime().availableProcessors();

    private final ConnectionCredentials validAuth;

    private ConnectionManager manager;
    private ResourceTestRule  resources;

    @Rule public TestName name = new TestName();

    public JMXProxyResourceTest() throws Exception {
        if (passwdFile != null) {
            String[] creds = new BufferedReader(new FileReader(new File(passwdFile))).readLine().split("\\s+");
            validAuth = new ConnectionCredentials(creds[0], creds[1]);
        } else {
            validAuth = null;
        }
    }

    private <T> T requestWithAuth(String path, Class<T> klass) {
        if (passwdFile == null) {
            return resources.client().target(path).request().get(klass);
        } else {
            return resources.client().target(path).request().post(Entity.json(validAuth), klass);
        }
    }

    @Rule
    public ResourceTestRule getResources() throws Exception {
        manager   = new ConnectionManager(new AppConfig());
        resources = ResourceTestRule.builder().addResource(new JMXProxyResource(manager)).build();

        return resources;
    }

    @Before
    public void printTestName() {
        System.out.println(" -> " + name.getMethodName());
    }

    @After
    public void shutdownManager() {
        manager.stop();
    }

    /* Config tests */
    @Test
    public void checkValidConfig() throws Exception {
        manager.getConfiguration()
            .setCleanInterval(Duration.milliseconds(12))
            .setCacheDuration(Duration.seconds(23))
            .setAccessDuration(Duration.seconds(404276))
            .setConnectTimeout(Duration.seconds(3))
            .setHistorySize(11)
            .setAllowedEndpoints(Arrays.asList("localhost:1100", "remotehost:2211"));

        ObjectMapper om = new ObjectMapper();
        String expected = om.writeValueAsString(om.readValue(FixtureHelpers.fixture("fixtures/web_config.json"), JsonNode.class));
        String acquired = resources.client().target("/config").request().get(String.class);
        assertTrue(expected.equals(acquired));
    }

    /* Whitelist tests */
    @Test
    public void checkValidHostNoWhitelist() throws Exception {
        List result = requestWithAuth("/" + validHost, List.class);
        assertTrue(result.contains(validMBean));
    }

    @Test
    public void checkValidHostWhitelist() throws Exception {
        manager.getConfiguration().setAllowedEndpoints(Arrays.asList(new String[] { validHost }));

        List result = requestWithAuth("/" + validHost, List.class);
        assertTrue(result.contains(validMBean));
    }

    @Test(expected=ForbiddenException.class)
    public void checkInvalidHostWhitelist() throws Exception {
        manager.getConfiguration().setAllowedEndpoints(Arrays.asList(new String[] { validHost }));

        resources.client().target("/" + invalidHost).request().get(List.class);
    }

    /* Auth tests */
    @Test
    public void checkValidAuthJsonHost() throws Exception {
        assumeNotNull(passwdFile);

        List result = resources.client().target("/" + validHost).request().post(Entity.json(validAuth), List.class);
        assertTrue(result.contains(validMBean));
    }
    @Test
    public void checkValidAuthJsonMBean() throws Exception {
        assumeNotNull(passwdFile);

        List result = resources.client().target("/" + validHost + "/" + validMBean).request().post(Entity.json(validAuth), List.class);
        assertTrue(result.contains(validAttribute));
    }
    @Test
    public void checkValidAuthJsonAttribute() throws Exception {
        assumeNotNull(passwdFile);

        int result = resources.client().target("/" + validHost + "/" + validMBean + "/" + validAttribute).request().post(Entity.json(validAuth), Integer.class);
        assertTrue(result == validValue);
    }

    @Test
    public void checkValidAuthFormHost() throws Exception {
        assumeNotNull(passwdFile);

        Form creds = new Form()
            .param("username", validAuth.getUsername())
            .param("password", validAuth.getPassword());
        List result = resources.client().target("/" + validHost).request().post(Entity.form(creds), List.class);
        assertTrue(result.contains(validMBean));
    }
    @Test
    public void checkValidAuthFormMBean() throws Exception {
        assumeNotNull(passwdFile);

        Form creds = new Form()
            .param("username", validAuth.getUsername())
            .param("password", validAuth.getPassword());
        List result = resources.client().target("/" + validHost + "/" + validMBean).request().post(Entity.form(creds), List.class);
        assertTrue(result.contains(validAttribute));
    }
    @Test
    public void checkValidAuthFormAttribute() throws Exception {
        assumeNotNull(passwdFile);

        Form creds = new Form()
            .param("username", validAuth.getUsername())
            .param("password", validAuth.getPassword());
        int result = resources.client().target("/" + validHost + "/" + validMBean + "/" + validAttribute).request().post(Entity.form(creds), Integer.class);
        assertTrue(result == validValue);
    }

    @Test
    public void checkCycledAuthJsonAttributePreserveHistory() throws Exception {
        assumeNotNull(passwdFile);

        manager.getConfiguration()
            .setHistorySize(5)
            .setCacheDuration(Duration.seconds(3));

        final ConnectionCredentials invalidAuth = new ConnectionCredentials(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        );

        int latest = resources.client().target("/" + validHost + "/" + validMBean + "/" + validAttribute).request().post(Entity.json(validAuth), Integer.class);
        assertTrue(latest == validValue);

        try {
            resources.client().target("/" + validHost + "/" + validMBean + "/" + validAttribute).request().post(Entity.json(invalidAuth), Integer.class);
            fail("failed to throw expected NotAuthorizedException");
        } catch(NotAuthorizedException e) {
            // successfully caught
        }

        java.lang.Thread.sleep(Duration.seconds(5).toMilliseconds());

        List result = resources.client().target("/" + validHost + "/" + validMBean + "/" + validAttribute + "?limit=2").request().post(Entity.json(validAuth), List.class);
        assertTrue(result.size() == 2);
    }
    @Test
    public void checkCycledAuthFormAttributePreserveHistory() throws Exception {
        assumeNotNull(passwdFile);

        manager.getConfiguration()
            .setHistorySize(5)
            .setCacheDuration(Duration.seconds(3));

        Form invalidAuth = new Form()
            .param("username", UUID.randomUUID().toString())
            .param("password", UUID.randomUUID().toString());

        int latest = resources.client().target("/" + validHost + "/" + validMBean + "/" + validAttribute).request().post(Entity.json(validAuth), Integer.class);
        assertTrue(latest == validValue);

        try {
            resources.client().target("/" + validHost + "/" + validMBean + "/" + validAttribute).request().post(Entity.form(invalidAuth), Integer.class);
            fail("failed to throw expected NotAuthorizedException");
        } catch(NotAuthorizedException e) {
            // successfully caught
        }

        java.lang.Thread.sleep(Duration.seconds(5).toMilliseconds());

        List result = resources.client().target("/" + validHost + "/" + validMBean + "/" + validAttribute + "?limit=2").request().post(Entity.json(validAuth), List.class);
        assertTrue(result.size() == 2);
    }

    @Test(expected=NotAuthorizedException.class)
    public void checkInvalidAuthGet() {
        assumeNotNull(passwdFile);

        resources.client().target("/" + validHost).request().get(List.class);
    }

    @Test(expected=NotAuthorizedException.class)
    public void checkInvalidAuthJsonHost() {
        assumeNotNull(passwdFile);

        final ConnectionCredentials invalidAuth = new ConnectionCredentials(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        );
        resources.client().target("/" + validHost).request().post(Entity.json(invalidAuth), List.class);
    }
    @Test(expected=NotAuthorizedException.class)
    public void checkInvalidAuthJsonMBean() {
        assumeNotNull(passwdFile);

        final ConnectionCredentials invalidAuth = new ConnectionCredentials(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        );
        resources.client().target("/" + validHost + "/" + validMBean).request().post(Entity.json(invalidAuth), List.class);
    }
    @Test(expected=NotAuthorizedException.class)
    public void checkInvalidAuthJsonAttribute() {
        assumeNotNull(passwdFile);

        final ConnectionCredentials invalidAuth = new ConnectionCredentials(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        );
        resources.client().target("/" + validHost + "/" + validMBean + "/" + validAttribute).request().post(Entity.json(invalidAuth), Integer.class);
    }

    @Test(expected=ClientErrorException.class)
    public void checkInvalidAuthJsonNullHost() {
        assumeNotNull(passwdFile);

        resources.client().target("/" + validHost).request().post(Entity.json(null), List.class);
    }
    @Test(expected=ClientErrorException.class)
    public void checkInvalidAuthJsonNullMBean() {
        assumeNotNull(passwdFile);

        resources.client().target("/" + validHost + "/" + validMBean).request().post(Entity.json(null), List.class);
    }
    @Test(expected=ClientErrorException.class)
    public void checkInvalidAuthJsonNullAttribute() {
        assumeNotNull(passwdFile);

        resources.client().target("/" + validHost + "/" + validMBean + "/" + validAttribute).request().post(Entity.json(null), Integer.class);
    }

    @Test(expected=ClientErrorException.class)
    public void checkInvalidAuthJsonEmptyHost() {
        assumeNotNull(passwdFile);

        resources.client().target("/" + validHost).request().post(Entity.json(new HashMap()), List.class);
    }
    @Test(expected=ClientErrorException.class)
    public void checkInvalidAuthJsonEmptyMBean() {
        assumeNotNull(passwdFile);

        resources.client().target("/" + validHost + "/" + validMBean).request().post(Entity.json(new HashMap()), List.class);
    }
    @Test(expected=ClientErrorException.class)
    public void checkInvalidAuthJsonEmptyAttribute() {
        assumeNotNull(passwdFile);

        resources.client().target("/" + validHost + "/" + validMBean + "/" + validAttribute).request().post(Entity.json(new HashMap()), Integer.class);
    }

    @Test(expected=NotAuthorizedException.class)
    public void checkInvalidAuthFormHost() {
        assumeNotNull(passwdFile);

        Form invalidAuth = new Form()
            .param("username", UUID.randomUUID().toString())
            .param("password", UUID.randomUUID().toString());
        resources.client().target("/" + validHost).request().post(Entity.form(invalidAuth), List.class);
    }
    @Test(expected=NotAuthorizedException.class)
    public void checkInvalidAuthFormMBean() {
        assumeNotNull(passwdFile);

        Form invalidAuth = new Form()
            .param("username", UUID.randomUUID().toString())
            .param("password", UUID.randomUUID().toString());
        resources.client().target("/" + validHost + "/" + validMBean).request().post(Entity.form(invalidAuth), List.class);
    }
    @Test(expected=NotAuthorizedException.class)
    public void checkInvalidAuthFormAttribute() {
        assumeNotNull(passwdFile);

        Form invalidAuth = new Form()
            .param("username", UUID.randomUUID().toString())
            .param("password", UUID.randomUUID().toString());
        resources.client().target("/" + validHost + "/" + validMBean + "/" + validAttribute).request().post(Entity.form(invalidAuth), Integer.class);
    }

    @Test(expected=NotAuthorizedException.class)
    public void checkInvalidAuthFormNullHost() {
        assumeNotNull(passwdFile);

        Form invalidAuth = null;
        resources.client().target("/" + validHost).request().post(Entity.form(invalidAuth), List.class);
    }
    @Test(expected=NotAuthorizedException.class)
    public void checkInvalidAuthFormNullMBean() {
        assumeNotNull(passwdFile);

        Form invalidAuth = null;
        resources.client().target("/" + validHost + "/" + validMBean).request().post(Entity.form(invalidAuth), List.class);
    }
    @Test(expected=NotAuthorizedException.class)
    public void checkInvalidAuthFormNullAttribute() {
        assumeNotNull(passwdFile);

        Form invalidAuth = null;
        resources.client().target("/" + validHost + "/" + validMBean + "/" + validAttribute).request().post(Entity.form(invalidAuth), Integer.class);
    }

    @Test(expected=NotAuthorizedException.class)
    public void checkInvalidAuthFormEmptyHost() {
        assumeNotNull(passwdFile);

        Form invalidAuth = new Form();
        resources.client().target("/" + validHost).request().post(Entity.form(invalidAuth), List.class);
    }
    @Test(expected=NotAuthorizedException.class)
    public void checkInvalidAuthFormEmptyMBean() {
        assumeNotNull(passwdFile);

        Form invalidAuth = new Form();
        resources.client().target("/" + validHost + "/" + validMBean).request().post(Entity.form(invalidAuth), List.class);
    }
    @Test(expected=NotAuthorizedException.class)
    public void checkInvalidAuthFormEmptyAttribute() {
        assumeNotNull(passwdFile);

        Form invalidAuth = new Form();
        resources.client().target("/" + validHost + "/" + validMBean + "/" + validAttribute).request().post(Entity.form(invalidAuth), Integer.class);
    }

    /* Cache tests */
    @Test
    public void checkEmptyCache() throws Exception {
        List result = resources.client().target("/").request().get(List.class);
        assertTrue(result.isEmpty());
    }

    @Test
    public void checkValidEntry() throws Exception {
        List result;

        result = requestWithAuth("/" + validHost, List.class);
        assertTrue(result.contains(validMBean));

        result = resources.client().target("/").request().get(List.class);
        assertTrue(result.contains(validHost));
    }

    @Test
    public void checkDeleleValidHost() throws Exception {
        List result;

        result = requestWithAuth("/" + validHost, List.class);
        assertTrue(result.contains(validMBean));

        result = resources.client().target("/").request().get(List.class);
        assertTrue(result.contains(validHost));

        assertTrue(resources.client().target("/" + validHost).request().delete(Boolean.class));

        result = resources.client().target("/").request().get(List.class);
        assertTrue(result.isEmpty());
    }

    @Test(expected=NotFoundException.class)
    public void checkDeleleInvalidHost() throws Exception {
        resources.client().target("/" + invalidHost).request().delete(Boolean.class);
    }

    @Test
    public void checkHostExpiration() throws Exception {
        manager.getConfiguration()
            .setCleanInterval(Duration.seconds(2))
            .setAccessDuration(Duration.seconds(5))
            .setCacheDuration(Duration.seconds(30));
        manager.start();

        List result;

        result = requestWithAuth("/" + validHost, List.class);
        assertTrue(result.contains(validMBean));

        java.lang.Thread.sleep(Duration.seconds(3).toMilliseconds());

        result = requestWithAuth("/" + validHost, List.class);
        assertTrue(result.contains(validMBean));

        java.lang.Thread.sleep(Duration.seconds(8).toMilliseconds());

        result = resources.client().target("/").request().get(List.class);
        assertTrue(result.isEmpty());
    }


    /* Host tests */
    @Test
    public void checkValidHost() throws Exception {
        List result = requestWithAuth("/" + validHost, List.class);
        assertTrue(result.contains(validMBean));
    }

    @Test(expected=NotFoundException.class)
    public void checkInvalidHost() throws Exception {
        requestWithAuth("/" + invalidHost, List.class);
    }

    @Test
    public void checkValidHostFullTrue() throws Exception {
        Map result = requestWithAuth("/" + validHost + "?full=true", Map.class);
        assertTrue(result.containsKey(validMBean));
    }

    @Test
    public void checkValidHostFullFalse() throws Exception {
        List result = requestWithAuth("/" + validHost + "?full=false", List.class);
        assertTrue(result.contains(validMBean));
    }

    @Test(expected=BadRequestException.class)
    public void checkValidHostFullInvalid() throws Exception {
        requestWithAuth("/" + validHost + "?full=invalid", List.class);
    }

    @Test
    public void checkValidHostFullHistory() throws Exception {
        manager.getConfiguration()
            .setHistorySize(5)
            .setCacheDuration(Duration.seconds(3));

        Map result;

        result = requestWithAuth("/" + validHost + "?full=true&limit=2", Map.class);
        assertTrue(((List)((Map)result.get(validMBean)).get(validAttribute)).size() == 1);

        java.lang.Thread.sleep(Duration.seconds(5).toMilliseconds());

        result = requestWithAuth("/" + validHost + "?full=true&limit=2", Map.class);
        assertTrue(((List)((Map)result.get(validMBean)).get(validAttribute)).size() == 2);

        java.lang.Thread.sleep(Duration.seconds(5).toMilliseconds());

        result = requestWithAuth("/" + validHost + "?full=true&limit=2", Map.class);
        assertTrue(((List)((Map)result.get(validMBean)).get(validAttribute)).size() == 2);

        result = requestWithAuth("/" + validHost + "?full=true&limit=0", Map.class);
        assertTrue(((List)((Map)result.get(validMBean)).get(validAttribute)).size() > 2);

        result = requestWithAuth("/" + validHost + "?full=true&limit=-1", Map.class);
        assertTrue(((Integer)((Map)result.get(validMBean)).get(validAttribute)).equals(validValue));
    }

    @Test(expected=NotFoundException.class)
    public void checkValidHostInvalidLimit() throws Exception {
        requestWithAuth("/" + validHost + "?limit=invalid", List.class);
    }

    /* MBean tests */
    @Test
    public void checkValidMBean() throws Exception {
        List result = requestWithAuth("/" + validHost + "/" + validMBean, List.class);
        assertTrue(result.contains(validAttribute));
    }

    @Test(expected=NotFoundException.class)
    public void checkInvalidMBean() throws Exception {
        requestWithAuth("/" + validHost + "/" + invalidMBean, List.class);
    }

    @Test(expected=NotFoundException.class)
    public void checkValidMBeanInvalidHost() throws Exception {
        requestWithAuth("/" + invalidHost + "/" + validMBean, List.class);
    }

    @Test
    public void checkValidMBeanFullTrue() throws Exception {
        Map result = requestWithAuth("/" + validHost + "/" + validMBean + "?full=true", Map.class);
        assertTrue(result.containsKey(validAttribute));
    }

    @Test
    public void checkValidMBeanFullFalse() throws Exception {
        List result = requestWithAuth("/" + validHost + "/" + validMBean + "?full=false", List.class);
        assertTrue(result.contains(validAttribute));
    }

    @Test(expected=BadRequestException.class)
    public void checkValidMBeanFullInvalid() throws Exception {
        requestWithAuth("/" + validHost + "/" + validMBean + "?full=invalid", List.class);
    }

    @Test
    public void checkValidMBeanFullHistory() throws Exception {
        manager.getConfiguration()
            .setHistorySize(5)
            .setCacheDuration(Duration.seconds(3));

        Map result;

        result = requestWithAuth("/" + validHost + "/" + validMBean + "?full=true&limit=2", Map.class);
        assertTrue(((List)result.get(validAttribute)).size() == 1);

        java.lang.Thread.sleep(Duration.seconds(5).toMilliseconds());

        result = requestWithAuth("/" + validHost + "/" + validMBean + "?full=true&limit=2", Map.class);
        assertTrue(((List)result.get(validAttribute)).size() == 2);

        java.lang.Thread.sleep(Duration.seconds(5).toMilliseconds());

        result = requestWithAuth("/" + validHost + "/" + validMBean + "?full=true&limit=2", Map.class);
        assertTrue(((List)result.get(validAttribute)).size() == 2);

        result = requestWithAuth("/" + validHost + "/" + validMBean + "?full=true&limit=0", Map.class);
        assertTrue(((List)result.get(validAttribute)).size() > 2);

        result = requestWithAuth("/" + validHost + "/" + validMBean + "?full=true&limit=-1", Map.class);
        assertTrue((int)result.get(validAttribute) == validValue);
    }

    @Test(expected=NotFoundException.class)
    public void checkValidMBeanInvalidLimit() throws Exception {
        requestWithAuth("/" + validHost + "/" + validMBean + "?limit=invalid", List.class);
    }

    /* Attribute tests */
    @Test
    public void checkValidAttribute() throws Exception {
        int result = requestWithAuth("/" + validHost + "/" + validMBean + "/" + validAttribute, Integer.class);
        assertTrue(result == validValue);
    }

    @Test
    public void checkValidAttributeHistory() throws Exception {
        manager.getConfiguration()
            .setHistorySize(5)
            .setCacheDuration(Duration.seconds(3));

        List result;

        result = requestWithAuth("/" + validHost + "/" + validMBean + "/" + validAttribute + "?limit=2", List.class);
        assertTrue(result.size() == 1);

        java.lang.Thread.sleep(Duration.seconds(5).toMilliseconds());

        result = requestWithAuth("/" + validHost + "/" + validMBean + "/" + validAttribute + "?limit=2", List.class);
        assertTrue(result.size() == 2);

        java.lang.Thread.sleep(Duration.seconds(5).toMilliseconds());

        result = requestWithAuth("/" + validHost + "/" + validMBean + "/" + validAttribute + "?limit=2", List.class);
        assertTrue(result.size() == 2);

        result = requestWithAuth("/" + validHost + "/" + validMBean + "/" + validAttribute + "?limit=0", List.class);
        assertTrue(result.size() > 2);

        int latest = requestWithAuth("/" + validHost + "/" + validMBean + "/" + validAttribute + "?limit=-1", Integer.class);
        assertTrue(latest == validValue);
    }

    @Test(expected=NotFoundException.class)
    public void checkInvalidAttribute() throws Exception {
        requestWithAuth("/" + validHost + "/" + validMBean + "/" + invalidAttribute, String.class);
    }

    @Test(expected=NotFoundException.class)
    public void checkValidAttributeInvalidHost() throws Exception {
        requestWithAuth("/" + invalidHost + "/" + validMBean + "/" + validAttribute, String.class);
    }

    @Test(expected=NotFoundException.class)
    public void checkValidAttributeInvalidMBean() throws Exception {
        requestWithAuth("/" + validHost + "/" + invalidMBean + "/" + validAttribute, String.class);
    }

    @Test(expected=NotFoundException.class)
    public void checkValidAttributeInvalidLimit() throws Exception {
        requestWithAuth("/" + validHost + "/" + validMBean + "/" + validAttribute + "?limit=invalid", String.class);
    }
}
