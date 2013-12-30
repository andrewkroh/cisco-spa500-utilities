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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * JAX-RS resource that hosts the dynamically registered
 * {@link PhoneApplication}s. Each {@code PhoneApplication} is treated as a
 * JAX-RS sub-resource. The sub-resources are obtained referencing their
 * {@link PhoneApplication#getApplicationId()} values.
 *
 * <p/>
 * CDI Note: this class does not use constructor injection because Resteasy
 * expects a no-arg constructor. Resteasy performs injection when the class is
 * used.
 *
 * @author akroh
 */
@ApplicationScoped
@Path("/app")
public class PhoneApplicationResource
{
    @Inject
    private PhoneApplicationManager applicationManager;

    @Path("/{id}")
    public PhoneApplication getApplication(@PathParam("id") String id)
    {
        return applicationManager.getApplication(id);
    }
}