package com.github.mk23.jmxproxy.conf.tests;

import com.github.mk23.jmxproxy.conf.AppConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.util.Duration;

import java.io.IOException;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import static io.dropwizard.testing.FixtureHelpers.fixture;

public class AppConfigTest {
    private final ObjectMapper om = new ObjectMapper();

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

    /* Configuration tests */
    @Test
    public void checkValidConfiguration() throws Exception {
        final AppConfig appConfig = new AppConfig()
            .setCleanInterval(Duration.milliseconds(12))
            .setCacheDuration(Duration.seconds(23))
            .setAccessDuration(Duration.seconds(404276))
            .setConnectTimeout(Duration.seconds(3))
            .setHistorySize(11)
            .setAllowedEndpoints(Arrays.asList("localhost:1100", "remotehost:2211"));

        final String expectedResult = jsonFixture("fixtures/app_config.json");
        final String acquiredResult = asJson(appConfig);

        assertThat("valid configuration", acquiredResult, is(expectedResult));
    }
}
