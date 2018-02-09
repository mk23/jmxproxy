package com.github.mk23.jmxproxy.core.tests;

import com.github.mk23.jmxproxy.core.MBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MBeanTest {
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
    public void checkEmptyMBean() throws Exception {
        final MBean mbean = new MBean();
        final String expected = new String("{}");

        assertThat("check empty mbean", asJson(mbean), is(expected));
    }

    @Test
    public void checkEmptyMBeanAttributeList() throws Exception {
        final MBean mbean = new MBean();
        final String expected = new String("[]");

        assertThat("check empty mbean attribute list", asJson(mbean.getAttributes()), is(expected));
    }

    @Test
    public void checkMissingAttribute() throws Exception {
        final MBean mbean = new MBean();
        final String expected = new String("null");

        assertThat("check missing attribute", asJson(mbean.getAttribute("attribute")), is(expected));
    }

    @Test
    public void checkNonexistingAttribute() throws Exception {
        final MBean mbean = new MBean();
        final String expected = new String("false");

        assertThat("check attribute does not exist", asJson(mbean.hasAttribute("attribute")), is(expected));
    }

    @Test
    public void checkAddAttributeExists() throws Exception {
        final MBean mbean = new MBean();
        mbean.addAttribute("attribute", null);

        final String expected = new String("true");

        assertThat("check add attribute exists", asJson(mbean.hasAttribute("attribute")), is(expected));
    }

    @Test
    public void checkAddSingleDefaultAttributeFull() throws Exception {
        final MBean mbean = new MBean();
        mbean.addAttribute("attribute", null);

        final String expected = new String("{\"attribute\":null}");

        assertThat("check add single default attribute full", asJson(mbean), is(expected));
    }

    @Test
    public void checkAddSingleDefaultAttributeList() throws Exception {
        final MBean mbean = new MBean();
        mbean.addAttribute("attribute", null);

        final String expected = new String("[\"attribute\"]");

        assertThat("check add single default attribute list", asJson(mbean.getAttributes()), is(expected));
    }

    @Test
    public void checkAddSingleDefaultAttributeBare() throws Exception {
        final MBean mbean = new MBean();
        mbean.addAttribute("attribute", null);

        final String expected = new String("null");

        assertThat("check add single default attribute bare", asJson(mbean.getAttribute("attribute")), is(expected));
    }

    @Test
    public void checkAddMultiDefaultAttributeFull() throws Exception {
        final MBean mbean = new MBean();
        mbean.addAttribute("attribute", "val1");
        mbean.addAttribute("attribute", "val2");

        final String expected = new String("{\"attribute\":\"val2\"}");

        assertThat("check add multi default attribute full", asJson(mbean), is(expected));
    }

    @Test
    public void checkAddMultiDefaultAttributeBare() throws Exception {
        final MBean mbean = new MBean();
        mbean.addAttribute("attribute", "val1");
        mbean.addAttribute("attribute", "val2");

        final String expected = new String("\"val2\"");

        assertThat("check add multi default attribute bare", asJson(mbean.getAttribute("attribute")), is(expected));
    }

    @Test
    public void checkAddMultiDefaultAttributeHistoryFull() throws Exception {
        final MBean mbean = new MBean();
        mbean.addAttribute("attribute", "val1");
        mbean.addAttribute("attribute", "val2");
        mbean.setLimit(0);

        final String expected = new String("{\"attribute\":[\"val2\"]}");

        assertThat("check add single attribute default history full", asJson(mbean), is(expected));
    }

    @Test
    public void checkAddMultiExplicitAttributeHistoryFull() throws Exception {
        final MBean mbean = new MBean(1);
        mbean.addAttribute("attribute", "val1");
        mbean.addAttribute("attribute", "val2");
        mbean.setLimit(0);

        final String expected = new String("{\"attribute\":[\"val2\"]}");

        assertThat("check add multi explicit attribute history full", asJson(mbean), is(expected));
    }

    @Test
    public void checkAddMultiAttributeHistoryFull() throws Exception {
        final MBean mbean = new MBean(2);
        mbean.addAttribute("attribute", "val1");
        mbean.addAttribute("attribute", "val2");
        mbean.setLimit(0);

        final String expected = new String("{\"attribute\":[\"val2\",\"val1\"]}");

        assertThat("check add multiple attributes history full", asJson(mbean), is(expected));
    }

    @Test
    public void checkAddMultiAttributeHistoryFullLimit() throws Exception {
        final MBean mbean = new MBean(2);
        mbean.addAttribute("attribute", "val1");
        mbean.addAttribute("attribute", "val2");
        mbean.setLimit(1);

        final String expected = new String("{\"attribute\":[\"val2\"]}");

        assertThat("check add multiple attributes history full limited", asJson(mbean), is(expected));
    }

    @Test
    public void checkAddMultiAttributeHistoryBare() throws Exception {
        final MBean mbean = new MBean(2);
        mbean.addAttribute("attribute", "val1");
        mbean.addAttribute("attribute", "val2");

        final String expected = new String("[\"val2\",\"val1\"]");

        assertThat("check add multiple attributes history bare", asJson(mbean.getAttributes("attribute", 0)), is(expected));
    }

    @Test
    public void checkAddMultiAttributeHistoryBareLimit() throws Exception {
        final MBean mbean = new MBean(2);
        mbean.addAttribute("attribute", "val1");
        mbean.addAttribute("attribute", "val2");

        final String expected = new String("[\"val2\"]");

        assertThat("check add multiple attributes history bare limited", asJson(mbean.getAttributes("attribute", 1)), is(expected));
    }
}
