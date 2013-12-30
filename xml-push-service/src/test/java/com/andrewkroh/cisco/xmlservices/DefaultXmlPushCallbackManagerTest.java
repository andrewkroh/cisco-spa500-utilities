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

package com.andrewkroh.cisco.xmlservices;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

/**
 * Test for {@link DefaultXmlPushService}.
 *
 * @author akroh
 */
public class DefaultXmlPushCallbackManagerTest
{
    private final static String BASE_URL = "http://host/path";

    private final XmlPushCallback mockCallback = mock(XmlPushCallback.class);

    private final DefaultXmlPushCallbackManager manager =
            new DefaultXmlPushCallbackManager(BASE_URL);

    @Test(expected = NullPointerException.class)
    public void constructor_withNullUrl_throwsException()
    {
        new DefaultXmlPushCallbackManager(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_withUrlEndingInSlash_throwsException()
    {
        new DefaultXmlPushCallbackManager(BASE_URL + "/");
    }

    @Test
    public void constructor_initializesFields()
    {
        assertThat((String) Whitebox.getInternalState(manager, "baseCallbackUrl"),
                equalTo(BASE_URL));
    }

    @Test(expected = NullPointerException.class)
    public void getCallback_withNullCallbackId_throwsException()
    {
        manager.getCallback(null);
    }

    @Test(expected = NullPointerException.class)
    public void registerCallback_withNullcommandCallback_throwsException()
    {
        manager.registerCallback(null);
    }

    @Test
    public void unregisterCallback_withNullCommandCallback_returnsFalse()
    {
        assertFalse(manager.unregisterCallback(null));
    }

    @Test
    public void registerCallback_alreadyRegistered_returnsSameUrl()
    {
        assertThat(manager.registerCallback(mockCallback),
                equalTo(manager.registerCallback(mockCallback)));
    }

    @Test
    public void registerCallback_returnsBaseUrlPlusUuid()
    {
        String url = manager.registerCallback(mockCallback);

        // Example: http://host/path/48ba7e75-c84a-4497-8989-a9f0c64efaa5
        assertTrue(url.matches(BASE_URL + "/\\w+-\\w+-\\w+-\\w+-\\w+"));
    }

    @Test
    public void getCallback_returnsCallback()
    {
        String callbackUrl = manager.registerCallback(mockCallback);
        String callbackId = callbackUrl.substring(
                callbackUrl.lastIndexOf("/") + 1, callbackUrl.length());
        assertThat(manager.getCallback(callbackId),
                   sameInstance(mockCallback));
    }

    @Test
    public void unregister_withRegisteredCallback_returnsTrue()
    {
        manager.registerCallback(mockCallback);
        assertTrue(manager.unregisterCallback(mockCallback));
    }
}
