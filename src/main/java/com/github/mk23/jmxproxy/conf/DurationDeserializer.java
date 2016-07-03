package com.github.mk23.jmxproxy.conf;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import io.dropwizard.util.Duration;

import java.io.IOException;

/**
 * <p>Custom deserializer for configuration Duration fields.</p>
 *
 * Converts a String or Long into <code>io.dropwizard.util.Duration</code> types to milliseconds.
 *
 * @see <a href="http://dropwizard.github.io/dropwizard/0.9.3/dropwizard-util/apidocs/io/dropwizard/util/Duration.html">io.dropwizard.util.Duration</a>
 * @see <a href="https://fasterxml.github.io/jackson-databind/javadoc/2.6/com/fasterxml/jackson/databind/JsonDeserializer.html">com.fasterxml.jackson.databind.JsonDeserializer</a>
 *
 * @since   2016-07-02
 * @author  mk23
 * @version 3.3.3
 */
public class DurationDeserializer extends JsonDeserializer<Duration> {
    /** {@inheritDoc} */
    @Override
    public final Duration deserialize(
        final JsonParser jp,
        final DeserializationContext ctxt
    ) throws IOException, JsonProcessingException {
        return jp.getCurrentToken() == JsonToken.VALUE_NUMBER_INT
            ? Duration.milliseconds(jp.getValueAsLong())
            : Duration.parse(jp.getValueAsString());
    }
}
