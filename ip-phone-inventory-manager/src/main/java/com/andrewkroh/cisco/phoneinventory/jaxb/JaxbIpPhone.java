/*
 * Copyright 2013 Andrew Kroh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andrewkroh.cisco.phoneinventory.jaxb;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * IP phone that is contained within the {@link JaxbPhoneInventory}.
 *
 * @author akroh
 */
public class JaxbIpPhone
{
    /**
     * Hostname or IP address of the phone.
     */
    @XmlElement(required = true)
    private final String hostname;

    /**
     * Port number of the IP phone's web server.
     */
    @XmlElement(required = true)
    private final int port;

    /**
     * Username for authenticating to the IP phone.
     */
    @XmlElement(required = true)
    private final String username;

    /**
     * Password for authenticating to the IP phone.
     */
    @XmlElement(required = true)
    private final String password;

    /**
     * Creates a new {@link BasicIpPhone} when the specified properties. All
     * fields are required.
     *
     * @param hostname
     *            hostname of the phone, cannot be {@code null}
     * @param port
     *            port of the phone's web server, cannot be {@code null} or
     *            negative
     * @param username
     *            username for authenticating to the phone, cannot be
     *            {@code null} or blank
     * @param password
     *            password for authenticating to the phone, cannot be
     *            {@code null} or blank
     *
     * @throws NullPointerException
     *             if any parameter is {@code null}
     */
    public JaxbIpPhone(String hostname, Integer port, String username,
            String password)
    {
        this.hostname = checkNotNull(hostname);
        this.port = checkNotNull(port);
        this.username = checkNotNull(username);
        this.password = checkNotNull(password);

        checkArgument(port >= 0, "Port must be non-negative.");
    }

    /**
     * No-arg constructor that is required for JAXB.
     */
    @SuppressWarnings("unused")
    private JaxbIpPhone()
    {
        this.hostname = null;
        this.port = -1;
        this.username = null;
        this.password = null;
    }

    public String getHostname()
    {
        return hostname;
    }

    public int getPort()
    {
        return port;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                    .append("hostname", hostname)
                    .append("port", port)
                    .append("username", username)
                    .build();
    }
}
