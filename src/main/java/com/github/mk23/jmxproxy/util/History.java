package com.github.mk23.jmxproxy.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Fixed size historical attribute value store.</p>
 *
 * Saves an object into a fixed size round robin store.  Allows clients to retreive latest
 * value, a subset of latest values, or the full stored history of values.  When number
 * of stored values exceeds the fixed size of the store, the oldest values are overwritten.
 *
 * @param <T> type of objects held in this History.
 *
 * @since   2015-05-11
 * @author  mk23
 * @version 3.3.2
 */
public class History<T> {
    private List<T> objects;
    private int length;
    private int pointer;
    private boolean wrapped;

    /**
     * <p>Default constructor.</p>
     *
     * Initializes the {@link Object} store {@link List} to the given history size.
     *
     * @param length number of {@link Object} values to keep in history.
     */
    public History(final int length) {
        this.length = length;

        pointer = -1;
        objects = new ArrayList<T>(Collections.nCopies(length, (T) null));
    }

    /**
     * <p>Adds a item to the store.</p>
     *
     * Adds a new item to the history store.  If the number of items exceeds the size
     * of the store, the oldest item is overwritten.
     *
     * @param item newest item to add to the store.
     */
    public final void add(final T item) {
        wrapped = ++pointer == length;
        pointer = pointer % length;

        objects.set(pointer, item);
    }

    /**
     * <p>Gets the latest item from the history store.</p>
     *
     * Gets the latest single item from the history store.
     *
     * @return Latest item from the history store or null if empty.
     */
    public final T getLast() {
        return pointer < 0 ? null : objects.get(pointer % length);
    }

    /**
     * <p>Gets the full item history from the store.</p>
     *
     * Gets the full item history from the store as a {@link List}.
     *
     * @return {@link List} of the full history.  May be empty if history has no items.
     */
    public final List<T> get() {
        return get(length);
    }

    /**
     * <p>Gets a limited item history from the store.</p>
     *
     * Gets a limited history from the store as a {@link List}, based
     * on requested item count.  If the specified count is less than 0 or greater than
     * the number of entries, a full history is returned instead.
     *
     * @param limit number of items to retreive from the history store.
     *
     * @return {@link List} of the requested slice of history.  May be empty if history has no items.
     */
    public final List<T> get(final int limit) {
        if (pointer < 0) {
            return Collections.emptyList();
        }

        int head = pointer + length * Boolean.compare(wrapped, false);
        int size = Math.min(limit > 0 ? limit : Integer.MAX_VALUE, Math.min(head + 1, length));
        List<T> rval = new ArrayList<T>(size);

        for (int i = 0; i < size; i++) {
            rval.add(objects.get((head - i) % length));
        }

        return rval;
    }
}
