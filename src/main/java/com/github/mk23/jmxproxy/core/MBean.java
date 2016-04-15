package com.github.mk23.jmxproxy.core;

import com.github.mk23.jmxproxy.util.History;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>JMX MBean tracker and serializer.</p>
 *
 * Maintains a map of JMX {@link Attribute} names and a {@link History} of
 * their values.  Implements JsonSerializable interface to convert the
 * stored map into JSON.
 *
 * @see <a href="https://fasterxml.github.io/jackson-databind/javadoc/2.6/com/fasterxml/jackson/databind/JsonSerializable.html">com.fasterxml.jackson.databind.JsonSerializable</a>
 *
 * @since   2015-05-11
 * @author  mk23
 * @version 3.2.0
 */
public class MBean implements JsonSerializable {
    private final Map<String, History<Attribute>> attributes;
    private final ThreadLocal<Integer> limit = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return -1;
        }
    };

    /**
     * <p>Default constructor.</p>
     *
     * Creates a map of {@link Attribute} name to {@link History} of associated values.
     */
    public MBean() {
        attributes = new HashMap<String, History<Attribute>>();
    }

    /**
     * <p>Inserts a new Attribute name to History association.</p>
     *
     * Optionally creates a new {@link History} object and inserts into the map store associating
     * it to the specified attribute name. Appends the specified {@link Attribute} value to the
     * {@link History}.
     *
     * @param attributeName name of the {@link Attribute} used as the map key.
     * @param attributeObject value of the {@link Attribute} added to history.
     * @param historySize number of items to preserve in the associated {@link History}.
     */
    public final void addAttribute(
        final String attributeName,
        final Object attributeObject,
        final int historySize
    ) {
        if (!attributes.containsKey(attributeName)) {
            attributes.put(attributeName, new History<Attribute>(historySize));
        }

        attributes.get(attributeName).add(new Attribute(attributeObject));
    }

    /**
     * <p>Sets the thread local history request limit.</p>
     *
     * Set the thread local limit for all {@link History} when serializing to JSON.
     * Because this method returns its object, requesting serialization can be done
     * with a single statement.
     *
     * <p>For example</p>
     *
     * <code>return Response.ok(mbean.setLimit(5)).build();</code>
     *
     * @see <a href="http://docs.oracle.com/javaee/7/api/javax/ws/rs/core/Response.html">javax.ws.rs.core.Response</a>
     *
     * @param bound the number of items to retreive from {@link History} for this thread.
     *
     * @return this mbean object for chaining calls.
     */
    public final MBean setLimit(final Integer bound) {
        limit.set(bound);
        return this;
    }

    /**
     * <p>Tester for attribute name.</p>
     *
     * Checks for requested {@link Attribute} name in the map store.
     *
     * @param attribute name of the {@link Attribute} to look up in the map store.
     *
     * @return true if the {@link Attribute} exists in the map store, false otherwise.
     */
    public final boolean hasAttribute(final String attribute) {
        return attributes.containsKey(attribute);
    }

    /**
     * <p>Getter for attribute names.</p>
     *
     * Extracts and returns the unique {@link Set} of all currently stored {@link Attribute} names.
     *
     * @return {@link Set} of {@link Attribute} name {@link String}s.
     */
    public final Set<String> getAttributes() {
        return attributes.keySet();
    }

    /**
     * <p>Getter for most recent attribute.</p>
     *
     * Fetches the most recent {@link Attribute} value for the specified name from the
     * associated {@link History} in the map store.
     *
     * @param attribute name of the {@link Attribute} to look up in the map store.
     *
     * @return latest {@link Attribute} object from {@link History} if found, null otherwise.
     */
    public final Attribute getAttribute(final String attribute) {
        if (hasAttribute(attribute)) {
            return attributes.get(attribute).getLast();
        }

        return null;
    }

    /**
     * <p>Getter for a subset of historical attribute values.</p>
     *
     * Fetches an array, limited by the requested bound, of most recent {@link Attribute} values
     * for the specified name from the associated {@link History} in the map store.
     *
     * @param attribute name of the {@link Attribute} to look up in the map store.
     * @param bound size of the resulting array or full history if this exceeds capacity or less than 1.
     *
     * @return {@link List} of the latest {@link Attribute} objects from {@link History} if found, empty otherwise.
     */
    public final List<Attribute> getAttributes(final String attribute, final int bound) {
        if (hasAttribute(attribute)) {
            return attributes.get(attribute).get(bound);
        }

        return Collections.emptyList();
    }

    /** {@inheritDoc} */
    @Override
    public final void serialize(
        final JsonGenerator jgen,
        final SerializerProvider sp
    ) throws IOException, JsonProcessingException {
        buildJson(jgen);
    }

    /** {@inheritDoc} */
    @Override
    public final void serializeWithType(
        final JsonGenerator jgen,
        final SerializerProvider sp,
        final TypeSerializer ts
    ) throws IOException, JsonProcessingException {
        buildJson(jgen);
    }

    private void buildJson(final JsonGenerator jgen) throws IOException, JsonProcessingException {
        int bound = limit.get();

        jgen.writeStartObject();
        for (Map.Entry<String, History<Attribute>> attributeEntry : attributes.entrySet()) {
            if (bound < 0) {
                jgen.writeObjectField(attributeEntry.getKey(), attributeEntry.getValue().getLast());
            } else {
                jgen.writeObjectField(attributeEntry.getKey(), attributeEntry.getValue().get(bound));
            }
        }
        jgen.writeEndObject();
    }
}
