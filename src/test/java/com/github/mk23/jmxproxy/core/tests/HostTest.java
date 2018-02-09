package com.github.mk23.jmxproxy.core.tests;

import com.github.mk23.jmxproxy.core.Host;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class HostTest {
    private final ObjectMapper om = new ObjectMapper();

    @Rule public TestName name = new TestName();

    private String asJson(Object object) throws JsonProcessingException {
        return om.writeValueAsString(object);
    }

    @Before
    public void printTestName() {
        System.out.println(" -> " + name.getMethodName());
    }

    @Test
    public void checkEmptyHost() throws Exception {
        final Host host = new Host();
        final String expected = new String("{}");

        assertThat("check empty host", asJson(host), is(expected));
    }

    @Test
    public void checkEmptyHostMBeanList() throws Exception {
        final Host host = new Host();
        final String expected = new String("[]");

        assertThat("check empty host mbean list", asJson(host.getMBeans()), is(expected));
    }

    @Test
    public void checkMissingMBean() throws Exception {
        final Host host = new Host();
        final String expected = new String("null");

        assertThat("check missing mbean", asJson(host.getMBean("bean")), is(expected));
    }

    @Test
    public void checkDuplicateMBean() throws Exception {
        final Host host = new Host();
        assertTrue(host.addMBean("bean") == host.addMBean("bean"));
    }

    @Test
    public void checkAddDefaultMBeanFull() throws Exception {
        final Host host = new Host();
        host.addMBean("bean");

        final String expected = new String("{\"bean\":{}}");

        assertThat("check add default mbean full", asJson(host), is(expected));
    }

    @Test
    public void checkAddDefaultMBeanList() throws Exception {
        final Host host = new Host();
        host.addMBean("bean");

        final String expected = new String("[\"bean\"]");

        assertThat("check add default mbean list", asJson(host.getMBeans()), is(expected));
    }

    @Test
    public void checkAddDefaultMBeanBare() throws Exception {
        final Host host = new Host();
        host.addMBean("bean");

        final String expected = new String("{}");

        assertThat("check add default mbean bare", asJson(host.getMBean("bean")), is(expected));
    }

    @Test
    public void checkAddExplicitMBeanFull() throws Exception {
        final Host host = new Host();
        host.addMBean("bean", 1);

        final String expected = new String("{\"bean\":{}}");

        assertThat("check add explicit mbean full", asJson(host), is(expected));
    }

    @Test
    public void checkAddExplicitMBeanList() throws Exception {
        final Host host = new Host();
        host.addMBean("bean", 1);

        final String expected = new String("[\"bean\"]");

        assertThat("check add explicit mbean list", asJson(host.getMBeans()), is(expected));
    }

    @Test
    public void checkAddExplicitMBeanBare() throws Exception {
        final Host host = new Host();
        host.addMBean("bean", 1);

        final String expected = new String("{}");

        assertThat("check add explicit mbean bare", asJson(host.getMBean("bean")), is(expected));
    }

    @Test
    public void checkRemoveMBean() throws Exception {
        final Host host = new Host();
        host.addMBean("bean");
        host.removeMBean("bean");

        final String expected = new String("{}");

        assertThat("check remove mbean", asJson(host), is(expected));
    }
}
