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

package com.andrewkroh.cisco.server.phone.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import javax.enterprise.inject.Instance;

import org.junit.Test;

/**
 * Unit test for {@link PhoneApplicationManager}.
 *
 * @author akroh
 */
public class PhoneApplicationManagerTest
{
    /**
     * Name of the one application that is registered with this
     * manager under test.
     */
    private static final String APP_NAME = "name";

    /**
     * {@link PhoneApplicationManager} under test.
     */
    private final PhoneApplicationManager manager;

    /**
     * Constructs a new {@link PhoneApplicationManager} and initializes
     * it with a single {@link PhoneApplication}.
     */
    @SuppressWarnings("unchecked")
    public PhoneApplicationManagerTest()
    {
        PhoneApplication app = mock(PhoneApplication.class);
        when(app.getApplicationId()).thenReturn(APP_NAME);

        Iterator<PhoneApplication> itr = mock(Iterator.class);
        when(itr.next()).thenReturn(app);
        when(itr.hasNext()).thenReturn(true).thenReturn(false);

        Instance<PhoneApplication> apps = mock(Instance.class);
        when(apps.iterator()).thenReturn(itr);

        manager = new PhoneApplicationManager(apps);
    }

    @Test
    public void getApplication_withKnownName_returnsApp()
    {
        assertThat(manager.getApplication(APP_NAME),
                hasProperty("applicationId", equalTo(APP_NAME)));
    }

    @Test
    public void getApplication_withUnknownName_returnsNull()
    {
        assertThat(manager.getApplication(APP_NAME + "unknown"), nullValue());
    }
}
