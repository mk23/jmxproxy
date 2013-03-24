package com.topsy.jmxproxy.jmx.tests;

import com.topsy.jmxproxy.jmx.ConnectionCredentials;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ConnectionCredentialsTest {
    @Test
    public void compareEquals() throws Exception {
        final ConnectionCredentials auth1 = new ConnectionCredentials(new String("user"), new String("pass"));
        final ConnectionCredentials auth2 = new ConnectionCredentials(new String("user"), new String("pass"));

        assertTrue(auth1.equals(auth2));
    }

    @Test
    public void compareNotEquals() throws Exception {
        final ConnectionCredentials auth1 = new ConnectionCredentials(new String("user"), new String("pass"));
        final ConnectionCredentials auth2 = new ConnectionCredentials(new String("pass"), new String("user"));

        assertFalse(auth1.equals(auth2));
    }

    @Test
    public void compareNotEqualsNull() throws Exception {
        final ConnectionCredentials auth = new ConnectionCredentials(new String("user"), new String("pass"));

        assertFalse(auth.equals(null));
    }
}
