package com.topsy.jmxproxy.jmx;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.NotEmpty;

public class ConnectionCredentials {
    @NotEmpty
    @JsonProperty
    private final String username;

    @NotEmpty
    @JsonProperty
    private final String password;

    public ConnectionCredentials(@JsonProperty("username") String username, @JsonProperty("password") String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object peer) {
        if (peer == null || !(peer instanceof ConnectionCredentials)) {
            return false;
        }

        ConnectionCredentials auth = (ConnectionCredentials) peer;
        return username.equals(auth.getUsername()) && password.equals(auth.getPassword());
    }
}
