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

package com.andrewkroh.cisco.rtp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.andrewkroh.cicso.rtp.Destination;

/**
 * Test for {@link Destination}.
 *
 * @author akroh
 */
public class DestinationTest
{
    private static final String HOST = "localhost";

    private static final int PORT = 10000;

    @Test(expected = NullPointerException.class)
    public void constructor_withNullHost_throwsException()
    {
        new Destination(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_withInvalidPortZero_throwsException()
    {
        new Destination("host", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_withInvalidPort_throwsException()
    {
        new Destination("host", 65536);
    }

    @Test
    public void constructor_setsAllFields()
    {
        Destination dest = new Destination(HOST, PORT);

        assertThat(dest.getHost(), equalTo(HOST));
        assertThat(dest.getPort(), equalTo(PORT));

        assertThat(dest.getSocketAddress(), notNullValue());
        assertThat(dest.getSocketAddress().getHostName(), equalTo(HOST));
        assertThat(dest.getSocketAddress().getPort(), equalTo(PORT));
    }

    @Test
    public void equals_returnsTrueForLikeObjects()
    {
        Destination one = new Destination(HOST, PORT);
        Destination two = new Destination(HOST, PORT);

        assertTrue(one.equals(two));
    }

    @Test
    public void hashCode_returnsSameValueForLikeObjects()
    {
        Destination one = new Destination(HOST, PORT);
        Destination two = new Destination(HOST, PORT);

        assertTrue(one.hashCode() == two.hashCode());
    }
}
