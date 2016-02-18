package com.github.mk23.jmxproxy.core;

/**
 * <p>Fixed size historical attribute value store.</p>
 *
 * Saves a JMX {@link Attribute} value object into a fixed size round robin store.
 * Allows clients to retreive latest value, a subset of latest values, or the
 * full stored history of values.  When number of stored values exceeds the fixed
 * size of the store, the oldest values are overwritten.
 *
 * @author  mk23
 * @since   2015-05-11
 * @version 3.2.0
 */
public class History {
    private Attribute[] attributes;
    private int current;

    /**
     * <p>Default constructor.</p>
     *
     * Initializes the {@link Attribute} store array to the given history size.
     *
     * @param size number of {@link Attribute} values to keep in history.
     */
    public History(final int size) {
        attributes = new Attribute[size];
    }

    /**
     * <p>Adds a JMX Attribute value to the store.</p>
     *
     * Adds a new JMX {@link Attribute} value to the history store.  If the
     * number of values exceeds the size of the store, the oldest value is
     * overwritten.
     *
     * @param attributeValue Object of the newest value to add to the store.
     */
    public final void addAttributeValue(final Object attributeValue) {
        Attribute attribute = new Attribute(attributeValue);
        attributes[++current % attributes.length] = attribute;
        if (current == Integer.MAX_VALUE) {
            current = current % attributes.length + attributes.length;
        }
    }

    /**
     * <p>Gets the latest attribute value from the history store.</p>
     *
     * Gets the latest single {@link Attribute} value from the history store.
     *
     * @return Latest value from the history store or null if empty.
     */
    public final Attribute getAttribute() {
        return attributes[current % attributes.length];
    }

    /**
     * <p>Gets the full attribute value history from the store.</p>
     *
     * Gets the full {@link Attribute} value history from the store as an array.
     *
     * @return Array of the full value history.  May be empty if history has no items.
     */
    public final Attribute[] getAttributes() {
        return getAttributes(attributes.length);
    }

    /**
     * <p>Gets a limited attribute value history from the store.</p>
     *
     * Gets a limited {@link Attribute} value history from the store as an array, based
     * on requested item count.  If the specified count is 0 or greater than the number
     * of entries, a full history is returned instead.
     *
     * @param limit number of values to retreive from the history store.
     *
     * @return Array of the requested value history.  May be empty if history has no items.
     */
    public final Attribute[] getAttributes(final int limit) {
        int size = Math.min(limit != 0 ? limit : Integer.MAX_VALUE, Math.min(current, attributes.length));
        Attribute[] rval = new Attribute[size];

        for (int i = 0; i < size; i++) {
            rval[i] = attributes[(current - i) % attributes.length];
        }

        return rval;
    }
}
