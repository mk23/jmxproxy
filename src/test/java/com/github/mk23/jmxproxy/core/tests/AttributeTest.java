package com.github.mk23.jmxproxy.core.tests;

import com.github.mk23.jmxproxy.core.Attribute;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.math.BigInteger;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import javax.management.ObjectName;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static io.dropwizard.testing.FixtureHelpers.fixture;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AttributeTest {
    private final ObjectMapper om   = new ObjectMapper();

    @Rule public TestName name = new TestName();

    private String asJson(Object object) throws JsonProcessingException {
        return om.writeValueAsString(object);
    }

    private String jsonFixture(String filename) throws IOException {
        return om.writeValueAsString(om.readValue(fixture(filename), JsonNode.class));
    }

    @Before
    public void printTestName() {
        System.out.println(" -> " + name.getMethodName());
    }

    @Test
    public void checkBoolean() throws Exception {
        final Attribute attribute = new Attribute(true);
        final String expected = new String("true");
        final String acquired = asJson(attribute);

        assertThat("check boolean serialization", asJson(attribute), is(expected));
    }

    @Test
    public void checkBoxedBooleanArray() throws Exception {
        final Attribute attribute = new Attribute(new Boolean[]{true, !true});
        final String expected = jsonFixture("fixtures/boxed_boolean_array.json");
        final String acquired = asJson(attribute);

        assertThat("check boolean array serialization", asJson(attribute), is(expected));
    }

    @Test
    public void checkInt() throws Exception {
        final Attribute attribute = new Attribute(1);
        final String expected = new String("1");

        assertThat("check int serialization", asJson(attribute), is(expected));
    }

    @Test
    public void checkNegativeInt() throws Exception {
        final Attribute attribute = new Attribute(-1);
        final String expected = new String("-1");

        assertThat("check negative int serialization", asJson(attribute), is(expected));
    }

    @Test
    public void checkBigInt() throws Exception {
        final Attribute attribute = new Attribute(new BigInteger("36893488147419103232"));
        final String expected = new String("36893488147419103232");

        assertThat("check big int serialization", asJson(attribute), is(expected));
    }

    @Test
    public void checkNegativeBigInt() throws Exception {
        final Attribute attribute = new Attribute(new BigInteger("-36893488147419103232"));
        final String expected = new String("-36893488147419103232");

        assertThat("check negative big int serialization", asJson(attribute), is(expected));
    }

    @Test
    public void checkBoxedIntegerArray() throws Exception {
        final Attribute attribute = new Attribute(new Integer[]{1, 1 + 1});
        final String expected = jsonFixture("fixtures/boxed_integer_array.json");

        assertThat("check integer array serialization", asJson(attribute), is(expected));
    }

    @Test
    public void checkDouble() throws Exception {
        final Attribute attribute = new Attribute(1.23);
        final String expected = new String("1.23");

        assertThat("check double serialization", asJson(attribute), is(expected));
    }

    @Test
    public void checkNegativeDouble() throws Exception {
        final Attribute attribute = new Attribute(-1.23);
        final String expected = new String("-1.23");

        assertThat("check negative double serialization", asJson(attribute), is(expected));
    }

    @Test
    public void checkBoxedDoubleArray() throws Exception {
        final Attribute attribute = new Attribute(new Double[]{1.23, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY});
        final String expected = jsonFixture("fixtures/boxed_double_array.json");

        assertThat("check double array serialization", asJson(attribute), is(expected));
    }

    @Test
    public void checkListInteger() throws Exception {
        final Attribute attribute = new Attribute(Arrays.asList(1, 2));
        final String expected = jsonFixture("fixtures/list_integer.json");

        assertThat("check integer list serialization", asJson(attribute), is(expected));
    }

    @Test
    public void checkListListInteger() throws Exception {
        final Attribute attribute = new Attribute(Arrays.asList(Arrays.asList(1, 2), Arrays.asList(3, 4)));
        final String expected = jsonFixture("fixtures/list_list_integer.json");

        assertThat("check list of integer lists serialization", asJson(attribute), is(expected));
    }

    @Test
    public void checkString() throws Exception {
        final Attribute attribute = new Attribute("val");
        final String expected = new String("\"val\"");

        assertThat("check string serialization", asJson(attribute), is(expected));
    }


    @Test
    public void checkStringArray() throws Exception {
        final Attribute attribute = new Attribute(new String[]{"val1", "val2"});
        final String expected = jsonFixture("fixtures/list_string.json");

        assertThat("check string array serialization", asJson(attribute), is(expected));
    }

    @Test
    public void checkListString() throws Exception {
        final Attribute attribute = new Attribute(Arrays.asList("val1", "val2"));
        final String expected = jsonFixture("fixtures/list_string.json");

        assertThat("check string list serialization", asJson(attribute), is(expected));
    }

    @Test
    public void checkListListString() throws Exception {
        final Attribute attribute = new Attribute(Arrays.asList(Arrays.asList("val1", "val2"), Arrays.asList("val3", "val4")));
        final String expected = jsonFixture("fixtures/list_list_string.json");

        assertThat("check list of string lists serialization", asJson(attribute), is(expected));
    }

    @Test
    public void checkJsonStringString() throws Exception {
        final Attribute attribute = new Attribute("\n\"val\"\n");
        final String expected = new String("\"val\"");

        assertThat("check single json string serialization", asJson(attribute), is(expected));
    }

    @Test
    public void checkJsonStringInteger() throws Exception {
        final Attribute attribute = new Attribute("1");
        final String expected = new String("1");

        assertThat("check single json string serialization", asJson(attribute), is(expected));
    }

    @Test
    public void checkJsonStringNested() throws Exception {
        final Attribute attribute = new Attribute("null true 1 1.23 \"val\" {\"key1\": [\"val1\", [1, 2]], \"key2\": \"val2\"} [1, 1.23]");
        final String expected = jsonFixture("fixtures/multi_json_string.json");

        assertThat("check multiple json strings serialization", asJson(attribute), is(expected));
    }

    @Test
    public void checkNull() throws Exception {
        final Attribute attribute = new Attribute(null);
        final String expected = new String("null");

        assertThat("check null serialization", asJson(attribute), is(expected));
    }
}
