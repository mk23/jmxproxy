package com.github.mk23.jmxproxy.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;

import java.lang.reflect.Array;

import java.util.List;
import java.util.ArrayList;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>JMX Attribute tracker and serializer.</p>
 *
 * Saves a JMX Attribute value object. Implements JsonSerializable
 * interface to convert the stored value into JSON. On serialization,
 * recursively inspects the value type and marshals it to JSON using
 * the supplied JsonGenerator. For {@link java.lang.reflect.Array},
 * {@link java.lang.Iterable}, or {@link javax.management.openmbean.TabularData},
 * builds a JSON array.  For {@link javax.management.openmbean.CompositeData},
 * builds a JSON object. For any other native types or null values, builds
 * a JSON equivalent.  Special cases:
 * <ul>
 *   <li>
 *   Any {@link java.lang.String} that contains valid JSON, will also be
 *   recursively serialized using a dynamic JsonParser.
 *   </li>
 *   <li>
 *   Any <code>NaN</code> {@link java.lang.Double} or {@link java.lang.Float}
 *   value will yield a JSON string <code>"NaN"</code>.
 *   </li>
 *   <li>
 *   Any <code>infinite</code> {@link java.lang.Double} or {@link java.lang.Float}
 *   value will yield a JSON string <code>"Infinity"</code>.
 *   </li>
 * </ul>
 *
 * @see <a href="http://fasterxml.github.io/jackson-core/javadoc/2.6/com/fasterxml/jackson/core/JsonGenerator.html">com.fasterxml.jackson.core.JsonGenerator</a>
 * @see <a href="http://fasterxml.github.io/jackson-core/javadoc/2.6/com/fasterxml/jackson/core/JsonParser.html">com.fasterxml.jackson.core.JsonParser</a>
 * @see <a href="https://fasterxml.github.io/jackson-databind/javadoc/2.6/com/fasterxml/jackson/databind/JsonSerializable.html">com.fasterxml.jackson.databind.JsonSerializable</a>
 *
 * @author  mk23
 * @since   2015-05-11
 * @version 3.2.0
 */
public class Attribute implements JsonSerializable {
    private static final Logger LOG = LoggerFactory.getLogger(Attribute.class);

    private Object attributeValue;

    /**
     * <p>Default constructor.</p>
     *
     * Saves the JMX Attribute object for later serialization.
     *
     * @param attributeValue object for later serialization.
     */
    public Attribute(final Object attributeValue) {
        this.attributeValue = attributeValue;
    }

    /** <p>Getter for attributeValue.</p>
     *
     * @return JMX Attribute value object.
     */
    public final Object getAttributeValue() {
        return attributeValue;
    }

    /** {@inheritDoc} */
    @Override
    public final void serialize(
        final JsonGenerator jgen,
        final SerializerProvider sp
    ) throws IOException, JsonProcessingException {
        buildJson(jgen, attributeValue);
    }

    /** {@inheritDoc} */
    @Override
    public final void serializeWithType(
        final JsonGenerator jgen,
        final SerializerProvider sp,
        final TypeSerializer ts
    ) throws IOException, JsonProcessingException {
        buildJson(jgen, attributeValue);
    }

    /**
     * <p>JMX Attribute value JSON serializer.</p>
     *
     * Inspects the stored JMX Attribute value type and serializes it to
     * JSON using the supplied JsonGenerator. Recursively calls itself
     * if finding JMX collections.  For an {@link java.lang.reflect.Array},
     * {@link java.lang.Iterable}, or {@link javax.management.openmbean.TabularData},
     * builds a JSON array.  For {@link  javax.management.openmbean.CompositeData},
     * builds a JSON object.  For any other native types or null values, builds
     * a JSON equivalent.  Special cases:
     * <ul>
     *   <li>
     *   Any {@link java.lang.String} that contains valid JSON, will also be
     *   recursively serialized using a dynamic JsonParser.
     *   </li>
     *   <li>
     *   Any <code>NaN</code> {@link java.lang.Double} or {@link java.lang.Float}
     *   value will yield a JSON string <code>"NaN"</code>.
     *   </li>
     *   <li>
     *   Any <code>infinite</code> {@link java.lang.Double} or {@link java.lang.Float}
     *   value will yield a JSON string <code>"Infinity"</code>.
     *   </li>
     * </ul>
     *
     * @see <a href="http://fasterxml.github.io/jackson-core/javadoc/2.6/com/fasterxml/jackson/core/JsonGenerator.html">com.fasterxml.jackson.core.JsonGenerator</a>
     * @see <a href="http://fasterxml.github.io/jackson-core/javadoc/2.6/com/fasterxml/jackson/core/JsonParser.html">com.fasterxml.jackson.core.JsonParser</a>
     *
     * @param jgen The jersey-supplied JSON generator to use for serialization.
     * @param objectValue The JMX Attribute value or an element of a collection to serialize.
     */
    private void buildJson(
        final JsonGenerator jgen,
        final Object objectValue
    ) throws IOException, JsonProcessingException {
        if (objectValue == null) {
            jgen.writeNull();
        } else if (objectValue instanceof Boolean) {
            jgen.writeBoolean((Boolean) objectValue);
        } else if (objectValue instanceof JsonNode) {
            jgen.writeTree((JsonNode) objectValue);
        } else if (objectValue.getClass().isArray()) {
            jgen.writeStartArray();
            int length = Array.getLength(objectValue);
            for (int i = 0; i < length; i++) {
                buildJson(jgen, Array.get(objectValue, i));
            }
            jgen.writeEndArray();
        } else if (objectValue instanceof Iterable) {
            Iterable data = (Iterable) objectValue;
            jgen.writeStartArray();
            for (Object objectEntry : data) {
                buildJson(jgen, objectEntry);
            }
            jgen.writeEndArray();
        } else if (objectValue instanceof TabularData) {
            TabularData data = (TabularData) objectValue;
            jgen.writeStartArray();
            for (Object objectEntry : data.values()) {
                buildJson(jgen, objectEntry);
            }
            jgen.writeEndArray();
        } else if (objectValue instanceof CompositeData) {
            CompositeData data = (CompositeData) objectValue;
            jgen.writeStartObject();
            for (String objectEntry : data.getCompositeType().keySet()) {
                jgen.writeFieldName(objectEntry);
                buildJson(jgen, data.get(objectEntry));
            }
            jgen.writeEndObject();
        } else if (objectValue instanceof Number) {
            Double data = ((Number) objectValue).doubleValue();
            if (data.isNaN()) {
                jgen.writeString("NaN");
            } else if (data.isInfinite()) {
                jgen.writeString("Infinity");
            } else {
                jgen.writeNumber(((Number) objectValue).toString());
            }
        } else {
            try {
                String input = objectValue.toString();
                List<JsonNode> parts = new ArrayList<JsonNode>();
                ObjectMapper mapper = new ObjectMapper();
                JsonParser parser = mapper.getFactory().createParser(input);

                for (parser.nextToken(); parser.hasCurrentToken(); parser.nextToken()) {
                    JsonToken tok = parser.getCurrentToken();
                    long pos = parser.getTokenLocation().getCharOffset();
                    if (tok == JsonToken.START_OBJECT || tok == JsonToken.START_ARRAY) {
                        parser.skipChildren();
                    }
                    long end = parser.getTokenLocation().getCharOffset()
                             + parser.getTextLength()
                             + (tok == JsonToken.VALUE_STRING ? 2 : 0);
                    parts.add(mapper.readTree(input.substring((int) pos, (int) end)));
                }

                if (parts.isEmpty()) {
                    jgen.writeString("");
                } else if (parts.size() == 1) {
                    buildJson(jgen, parts.get(0));
                } else {
                    buildJson(jgen, parts);
                }
            } catch (JsonParseException e) {
                jgen.writeString(objectValue.toString());
            }
        }
    }
}
