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

package com.andrewkroh.cicso.rtp;

import java.net.InetSocketAddress;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.common.base.Preconditions;

/**
 * This is a holder for a host/port pair.
 *
 * @author akroh
 */
public class Destination
{
    private final String host;

    private final int port;

    private final InetSocketAddress socketAddress;

    public Destination(String host, int port)
    {
        Preconditions.checkNotNull(host, "Host cannot be null.");
        Preconditions.checkArgument(port > 0 && port <= 65535,
                "Port number <%s> is not within (0, 65535].", port);

        this.host = host;
        this.port = port;
        this.socketAddress = new InetSocketAddress(host, port);
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

    public InetSocketAddress getSocketAddress()
    {
        return socketAddress;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(19, 53)
                    .append(host)
                    .append(port)
                    .build();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }

        if (obj == this)
        {
            return true;
        }

        if (obj.getClass() != getClass())
        {
            return false;
        }

        Destination rhs = (Destination) obj;
        return new EqualsBuilder()
                      .append(host, rhs.host)
                      .append(port, rhs.port)
                      .isEquals();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                    .append("host", host)
                    .append("port", port)
                    .build();
    }
}
