package com.github.mk23.jmxproxy.jmx;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * <p>JMX Username and Password user input POJO.</p>
 *
 * Used to marshal JMX agent credentials and provide methods for comparing.
 *
 * @since   2015-05-11
 * @author  mk23
 * @version 3.2.0
 */
public class ConnectionCredentials {
    @NotEmpty
    @JsonProperty
    private final String username;

    @NotEmpty
    @JsonProperty
    private final String password;

    private final String combined;

    /**
     * <p>Default constructor.</p>
     *
     * Sets the instance username and password {@link String}s from provided parameters.
     * Builds a single combined credential string for the hashCode computation.
     *
     * @param username username {@link String} for the new credential instance.
     * @param password password {@link String} for the new credential instance.
     */
    public ConnectionCredentials(
        @JsonProperty("username") final String username,
        @JsonProperty("password") final String password
    ) {
        this.username = username;
        this.password = password;
        this.combined = username + "\0" + password;
    }

    /**
     * <p>Getter for username.</p>
     *
     * Fetches the stored username {@link String}.
     *
     * @return username string.
     */
    public final String getUsername() {
        return username;
    }

    /**
     * <p>Getter for password.</p>
     *
     * Fetches the stored password {@link String}.
     *
     * @return password string.
     */
    public final String getPassword() {
        return password;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(final Object peer) {
        if (peer == null || !(peer instanceof ConnectionCredentials)) {
            return false;
        }

        ConnectionCredentials auth = (ConnectionCredentials) peer;
        return username.equals(auth.getUsername()) && password.equals(auth.getPassword());
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return combined.hashCode();
    }
}
