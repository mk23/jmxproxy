package com.topsy.jmxproxy.jmx.tests;

import com.topsy.jmxproxy.jmx.ConnectionCredentials;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ConnectionCredentialsTest {
    @Test
    public void compareEquals() throws Exception {
        final ConnectionCredentials auth1 = new ConnectionCredentials("user", "pass");
        final ConnectionCredentials auth2 = new ConnectionCredentials("user", "pass");

        assertTrue(auth1.equals(auth2));
    }

    @Test
    public void compareNotEquals() throws Exception {
        final ConnectionCredentials auth1 = new ConnectionCredentials("user", "pass");
        final ConnectionCredentials auth2 = new ConnectionCredentials("pass", "user");

        assertFalse(auth1.equals(auth2));
    }

    @Test
    public void compareNotEqualsNull() throws Exception {
        final ConnectionCredentials auth1 = new ConnectionCredentials("user", "pass");

        assertFalse(auth1.equals(null));
    }
}
