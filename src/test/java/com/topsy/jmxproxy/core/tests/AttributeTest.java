package com.topsy.jmxproxy.core.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.topsy.jmxproxy.JMXProxyConfiguration.JMXProxyApplicationConfiguration;
import com.topsy.jmxproxy.jmx.ConnectionManager;

import java.io.IOException;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import java.lang.management.ManagementFactory;

import javax.management.ObjectName;

import org.junit.Test;

import static io.dropwizard.testing.FixtureHelpers.fixture;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AttributeTest {
    private final ObjectMapper om = new ObjectMapper();

    private final String validHost  = "localhost:" + System.getProperty("com.sun.management.jmxremote.port");
    private final String validMBean = "AttributeTest:type=test";

    private final ConnectionManager manager = new ConnectionManager(new JMXProxyApplicationConfiguration());

    public interface AttributeTestJMXMBean {
        Object getNullValue();
        boolean getBooleanValue();
        Boolean[] getBoxedBooleanArrayValue();
        int getIntValue();
        Integer[] getBoxedIntegerArrayValue();
        double getDoubleValue();
        Double[] getBoxedDoubleArrayValue();
        List<Integer> getListIntegerValue();
        List<List<Integer>> getListListIntegerValue();
        String getStringValue();
        String[] getStringArrayValue();
        List<String> getListStringValue();
        List<List<String>> getListListStringValue();
        String getSingleJsonStringValue();
        String getMultiJsonStringValue();
    }

    public class AttributeTestJMX implements AttributeTestJMXMBean {
        public Object getNullValue() {
            return null;
        }

        public boolean getBooleanValue() {
            return true;
        }

        public Boolean[] getBoxedBooleanArrayValue() {
            boolean test = true;
            return new Boolean[]{test, !test};
        }

        public int getIntValue() {
            return 1;
        }

        public Integer[] getBoxedIntegerArrayValue() {
            int test = 1;
            return new Integer[]{test, test + 1};
        }

        public double getDoubleValue() {
            return 1.23;
        }

        public Double[] getBoxedDoubleArrayValue() {
            double test = 1.23;
            return new Double[]{test, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY};
        }

        public List<Integer> getListIntegerValue() {
            return Arrays.asList(1, 2);
        }

        public List<List<Integer>> getListListIntegerValue() {
            return Arrays.asList(Arrays.asList(1, 2), Arrays.asList(3, 4));
        }

        public String getStringValue() {
            return "val";
        }

        public String[] getStringArrayValue() {
            return new String[]{"val1", "val2"};
        }

        public List<String> getListStringValue() {
            return Arrays.asList("val1", "val2");
        }

        public List<List<String>> getListListStringValue() {
            return Arrays.asList(Arrays.asList("val1", "val2"), Arrays.asList("val3", "val4"));
        }

        public String getSingleJsonStringValue() {
            return " \"val\" ";
        }

        public String getMultiJsonStringValue() {
            return "null true 1 1.23 \"val\" {\"key1\": [\"val1\", [1, 2]], \"key2\": \"val2\"} [1, 1.23]";
        }
    }

    public AttributeTest() throws Exception {
        try {
            ManagementFactory.getPlatformMBeanServer().registerMBean(
                new AttributeTestJMX(), new ObjectName(validMBean)
            );
        } catch (javax.management.InstanceAlreadyExistsException e) {
        }
    }

    private String asJson(Object object) throws JsonProcessingException {
        return om.writeValueAsString(object);
    }

    private String jsonFixture(String filename) throws IOException {
        return om.writeValueAsString(om.readValue(fixture(filename), JsonNode.class));
    }

    /* Attribute tests */
    @Test
    public void checkNull() throws Exception {
        final String attributeKey = "NullValue";
        final String expectedResult = new String("null");
        final String acquiredResult = asJson(manager.getHost(validHost).getMBean(validMBean).getAttribute(attributeKey));

        assertThat("check null serialization", acquiredResult, is(expectedResult));
    }

    @Test
    public void checkBoolean() throws Exception {
        final String attributeKey = "BooleanValue";
        final String expectedResult = new String("true");
        final String acquiredResult = asJson(manager.getHost(validHost).getMBean(validMBean).getAttribute(attributeKey));

        assertThat("check boolean serialization", acquiredResult, is(expectedResult));
    }

    @Test
    public void checkBoxedBooleanArray() throws Exception {
        final String attributeKey = "BoxedBooleanArrayValue";
        final String expectedResult = jsonFixture("fixtures/boxed_boolean_array.json");
        final String acquiredResult = asJson(manager.getHost(validHost).getMBean(validMBean).getAttribute(attributeKey));

        assertThat("check boolean array serialization", acquiredResult, is(expectedResult));
    }

    @Test
    public void checkInt() throws Exception {
        final String attributeKey = "IntValue";
        final String expectedResult = new String("1");
        final String acquiredResult = asJson(manager.getHost(validHost).getMBean(validMBean).getAttribute(attributeKey));

        assertThat("check int serialization", acquiredResult, is(expectedResult));
    }

    @Test
    public void checkBoxedIntegerArray() throws Exception {
        final String attributeKey = "BoxedIntegerArrayValue";
        final String expectedResult = jsonFixture("fixtures/boxed_integer_array.json");
        final String acquiredResult = asJson(manager.getHost(validHost).getMBean(validMBean).getAttribute(attributeKey));

        assertThat("check integer array serialization", acquiredResult, is(expectedResult));
    }

    @Test
    public void checkDouble() throws Exception {
        final String attributeKey = "DoubleValue";
        final String expectedResult = new String("1.23");
        final String acquiredResult = asJson(manager.getHost(validHost).getMBean(validMBean).getAttribute(attributeKey));

        assertThat("check double serialization", acquiredResult, is(expectedResult));
    }

    @Test
    public void checkBoxedDoubleArray() throws Exception {
        final String attributeKey = "BoxedDoubleArrayValue";
        final String expectedResult = jsonFixture("fixtures/boxed_double_array.json");
        final String acquiredResult = asJson(manager.getHost(validHost).getMBean(validMBean).getAttribute(attributeKey));

        assertThat("check double array serialization", acquiredResult, is(expectedResult));
    }

    @Test
    public void checkListInteger() throws Exception {
        final String attributeKey = "ListIntegerValue";
        final String expectedResult = jsonFixture("fixtures/list_integer.json");
        final String acquiredResult = asJson(manager.getHost(validHost).getMBean(validMBean).getAttribute(attributeKey));

        assertThat("check integer list serialization", acquiredResult, is(expectedResult));
    }

    @Test
    public void checkListListInteger() throws Exception {
        final String attributeKey = "ListListIntegerValue";
        final String expectedResult = jsonFixture("fixtures/list_list_integer.json");
        final String acquiredResult = asJson(manager.getHost(validHost).getMBean(validMBean).getAttribute(attributeKey));

        assertThat("check list of integer lists serialization", acquiredResult, is(expectedResult));
    }

    @Test
    public void checkString() throws Exception {
        final String attributeKey = "StringValue";
        final String expectedResult = new String("\"val\"");
        final String acquiredResult = asJson(manager.getHost(validHost).getMBean(validMBean).getAttribute(attributeKey));

        assertThat("check string serialization", acquiredResult, is(expectedResult));
    }

    @Test
    public void checkStringArray() throws Exception {
        final String attributeKey = "StringArrayValue";
        final String expectedResult = jsonFixture("fixtures/list_string.json");
        final String acquiredResult = asJson(manager.getHost(validHost).getMBean(validMBean).getAttribute(attributeKey));

        assertThat("check string array serialization", acquiredResult, is(expectedResult));
    }

    @Test
    public void checkListString() throws Exception {
        final String attributeKey = "ListStringValue";
        final String expectedResult = jsonFixture("fixtures/list_string.json");
        final String acquiredResult = asJson(manager.getHost(validHost).getMBean(validMBean).getAttribute(attributeKey));

        assertThat("check string list serialization", acquiredResult, is(expectedResult));
    }

    @Test
    public void checkListListString() throws Exception {
        final String attributeKey = "ListListStringValue";
        final String expectedResult = jsonFixture("fixtures/list_list_string.json");
        final String acquiredResult = asJson(manager.getHost(validHost).getMBean(validMBean).getAttribute(attributeKey));

        assertThat("check list of string lists serialization", acquiredResult, is(expectedResult));
    }

    /*
    @Test
    public void checkSingleJsonString() throws Exception {
        final String attributeKey = "SingleJsonStringValue";
        final String expectedResult = new String("\"val\"");
        final String acquiredResult = asJson(manager.getHost(validHost).getMBean(validMBean).getAttribute(attributeKey));

        assertThat("check single json string serialization", acquiredResult, is(expectedResult));
    }
    */

    @Test
    public void checkMultiJsonString() throws Exception {
        final String attributeKey = "MultiJsonStringValue";
        final String expectedResult = jsonFixture("fixtures/multi_json_string.json");
        final String acquiredResult = asJson(manager.getHost(validHost).getMBean(validMBean).getAttribute(attributeKey));

        assertThat("check multiple json strings serialization", acquiredResult, is(expectedResult));
    }
}
