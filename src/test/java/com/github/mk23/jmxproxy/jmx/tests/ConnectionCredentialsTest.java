package com.github.mk23.jmxproxy.jmx.tests;

import com.github.mk23.jmxproxy.jmx.ConnectionCredentials;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ConnectionCredentialsTest {
    @Rule public TestName name = new TestName();

    @Before
    public void printTestName() {
        System.out.println(" -> " + name.getMethodName());
    }

    @Test
    public void checkAuthenticatedEquals() throws Exception {
        final ConnectionCredentials auth1 = new ConnectionCredentials(new String("user"), new String("pass"));
        final ConnectionCredentials auth2 = new ConnectionCredentials(new String("user"), new String("pass"));

        assertTrue(auth1.equals(auth2));
    }

    @Test
    public void checkAuthenticatedNotEqualsUser() throws Exception {
        final ConnectionCredentials auth1 = new ConnectionCredentials(new String("user"), new String("pass"));
        final ConnectionCredentials auth2 = new ConnectionCredentials(new String("pass"), new String("pass"));

        assertFalse(auth1.equals(auth2));
    }

    @Test
    public void checkAuthenticatedNotEqualsPass() throws Exception {
        final ConnectionCredentials auth1 = new ConnectionCredentials(new String("user"), new String("pass"));
        final ConnectionCredentials auth2 = new ConnectionCredentials(new String("user"), new String("user"));

        assertFalse(auth1.equals(auth2));
    }

    @Test
    public void checkAuthenticatedNotEqualsBoth() throws Exception {
        final ConnectionCredentials auth1 = new ConnectionCredentials(new String("user"), new String("pass"));
        final ConnectionCredentials auth2 = new ConnectionCredentials(new String("pass"), new String("user"));

        assertFalse(auth1.equals(auth2));
    }

    @Test
    public void checkAuthenticatedNotEqualsNull() throws Exception {
        final ConnectionCredentials auth = new ConnectionCredentials(new String("user"), new String("pass"));

        assertFalse(auth.equals(null));
    }

    @Test
    public void checkAuthenticatedNotEqualsInvalid() throws Exception {
        final ConnectionCredentials auth = new ConnectionCredentials(new String("user"), new String("pass"));

        assertFalse(auth.equals(new String("")));
    }

    @Test
    public void checkAuthenticatedEnabled() throws Exception {
        final ConnectionCredentials auth = new ConnectionCredentials(new String("user"), new String("pass"));

        assertTrue(auth.isEnabled());
    }

    @Test
    public void checkAuthenticatedHashCode() throws Exception {
        final ConnectionCredentials auth = new ConnectionCredentials(new String("user"), new String("pass"));

        assertTrue(auth.hashCode() == new String("user\0pass").hashCode());
    }

    @Test
    public void checkAnonymousEquals() throws Exception {
        final ConnectionCredentials auth1 = new ConnectionCredentials();
        final ConnectionCredentials auth2 = new ConnectionCredentials();

        assertTrue(auth1.equals(auth2));
    }

    @Test
    public void checkAnonymousNotEquals() throws Exception {
        final ConnectionCredentials auth1 = new ConnectionCredentials();
        final ConnectionCredentials auth2 = new ConnectionCredentials(new String("user"), new String("pass"));

        assertFalse(auth1.equals(auth2));
    }

    @Test
    public void checkAnonymousNotEqualsNull() throws Exception {
        final ConnectionCredentials auth = new ConnectionCredentials();

        assertTrue(auth.equals(null));
    }

    @Test
    public void checkAnonymousEnabledUser() throws Exception {
        final ConnectionCredentials auth = new ConnectionCredentials(new String("user"), null);

        assertFalse(auth.isEnabled());
    }

    @Test
    public void checkAnonymousEnabledPass() throws Exception {
        final ConnectionCredentials auth = new ConnectionCredentials(null, new String("pass"));

        assertFalse(auth.isEnabled());
    }

    @Test
    public void checkAnonymousEnabledBoth() throws Exception {
        final ConnectionCredentials auth = new ConnectionCredentials();

        assertFalse(auth.isEnabled());
    }

    @Test
    public void checkAnonymousHashCode() throws Exception {
        final ConnectionCredentials auth = new ConnectionCredentials();

        assertTrue(auth.hashCode() == 0);
    }
}
