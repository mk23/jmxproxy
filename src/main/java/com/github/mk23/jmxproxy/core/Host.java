package com.github.mk23.jmxproxy.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>JMX Host tracker and serializer.</p>
 *
 * Maintains a map of JMX {@link MBean} names and their values. Implements
 * JsonSerializable interface to convert the stored map into JSON.
 *
 * @see <a href="https://fasterxml.github.io/jackson-databind/javadoc/2.6/com/fasterxml/jackson/databind/JsonSerializable.html">com.fasterxml.jackson.databind.JsonSerializable</a>
 *
 * @since   2015-05-11
 * @author  mk23
 * @version 3.2.0
 */
public class Host implements JsonSerializable {
    private final Map<String, MBean> mbeans;
    private final ThreadLocal<Integer> limit = new ThreadLocal<Integer>() {
        @Override protected Integer initialValue() {
            return -1;
        }
    };

    /**
     * <p>Default constructor.</p>
     *
     * Creates a map of {@link MBean} name to associated values.
     */
    public Host() {
        mbeans = new HashMap<String, MBean>();
    }

    /**
     * <p>Inserts a new MBean name to value association.</p>
     *
     * Creates a new {@link MBean} object and inserts into the map store
     * associating it to the specified mbean name.
     *
     * @param mbeanName name of the {@link MBean} used as the map key.
     *
     * @return the newly created {@link MBean} object.
     */
    public final MBean addMBean(final String mbeanName) {
        if (!mbeans.containsKey(mbeanName)) {
            MBean mbean = new MBean();
            mbeans.put(mbeanName, mbean);
        }

        return mbeans.get(mbeanName);
    }

    /**
     * <p>Deletes an mbean association.</p>
     *
     * Removes an associated {@link MBean} from the map store for this host.
     *
     * @param mbeanName name of the {@link MBean} to remove.
     */
    public final void removeMBean(final String mbeanName) {
        mbeans.remove(mbeanName);
    }

    /**
     * <p>Sets the thread local history request limit.</p>
     *
     * Set the thread local limit for all {@link History} when serializing to JSON.
     * Because this method returns its object, requesting serialization can be done
     * with a single statement.
     *
     * <p>For example:</p>
     *
     * <code>return Response.ok(host.setLimit(5)).build();</code>
     *
     * @see <a href="http://docs.oracle.com/javaee/7/api/javax/ws/rs/core/Response.html">javax.ws.rs.core.Response</a>
     *
     * @param bound the number of items to retreive from {@link History} for this thread.
     *
     * @return this host object for chaining calls.
     */
    public final Host setLimit(final Integer bound) {
        limit.set(bound);
        return this;
    }

    /**
     * <p>Getter for mbean names.</p>
     *
     * Extracts and returns the unique {@link Set} of all currently stored {@link MBean} names.
     *
     * @return {@link Set} of {@link MBean} name {@link String}s.
     */
    public final Set<String> getMBeans() {
        return mbeans.keySet();
    }

    /**
     * <p>Getter for specific mbean.</p>
     *
     * Fetches the specified {@link MBean} from the map store.
     *
     * @param mbean name of the {@link MBean} to look up in the map store.
     *
     * @return {@link MBean} object if found, null otherwise.
     */
    public final MBean getMBean(final String mbean) {
        return mbeans.get(mbean);
    }

    /** {@inheritDoc} */
    public final void serialize(
        final JsonGenerator jgen,
        final SerializerProvider sp
    ) throws IOException, JsonProcessingException {
        buildJson(jgen);
    }

    /** {@inheritDoc} */
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
        for (Map.Entry<String, MBean> mbeanEntry : mbeans.entrySet()) {
            jgen.writeObjectField(mbeanEntry.getKey(), mbeanEntry.getValue().setLimit(bound));
        }
        jgen.writeEndObject();
    }
}
