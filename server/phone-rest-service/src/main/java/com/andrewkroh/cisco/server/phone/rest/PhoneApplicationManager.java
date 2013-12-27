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

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

/**
 * Manages all the available applications.
 *
 * @author akroh
 */
@Default
public class PhoneApplicationManager
{
    private static final Logger LOGGER =
            LoggerFactory.getLogger(PhoneApplicationManager.class);

    private final ImmutableMap<String, PhoneApplication> applications;

    @Inject
    PhoneApplicationManager(@Any Instance<PhoneApplication> apps)
    {
        ImmutableMap.Builder<String, PhoneApplication> builder =
                ImmutableMap.builder();
        for (PhoneApplication app : apps)
        {
            LOGGER.debug("Application registered: {}", app.getApplicationId());
            builder.put(app.getApplicationId(), app);
        }
        applications = builder.build();
    }

    public PhoneApplication getApplication(String id)
    {
        return applications.get(id);
    }
}
