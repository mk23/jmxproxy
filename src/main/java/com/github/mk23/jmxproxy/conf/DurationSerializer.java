package com.github.mk23.jmxproxy.conf;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.dropwizard.util.Duration;

import java.io.IOException;

/**
 * <p>Custom serializer for configuration Duration fields.</p>
 *
 * Converts <code>io.dropwizard.util.Duration</code> types to milliseconds
 * for JSON requests.
 *
 * @see <a href="http://dropwizard.github.io/dropwizard/0.9.2/dropwizard-util/apidocs/io/dropwizard/util/Duration.html">io.dropwizard.util.Duration</a>
 * @see <a href="https://fasterxml.github.io/jackson-databind/javadoc/2.6/com/fasterxml/jackson/databind/JsonSerializer.html">com.fasterxml.jackson.databind.JsonSerializer</a>
 *
 * @since   2016-01-29
 * @author  mk23
 * @version 3.2.1
 */
public class DurationSerializer extends JsonSerializer<Duration> {
    /** {@inheritDoc} */
    @Override
    public final void serialize(
        final Duration duration,
        final JsonGenerator jgen,
        final SerializerProvider provider
    ) throws IOException, JsonProcessingException {
        jgen.writeNumber(duration.toMilliseconds());
    }
}
