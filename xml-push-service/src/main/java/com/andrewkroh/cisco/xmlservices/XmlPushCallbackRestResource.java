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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * JAX-RS resource that hosts {@link XmlPushCallback}s as sub-resources.
 *
 * @author akroh
 */
@ApplicationScoped
@Path("/callback")
public class XmlPushCallbackRestResource
{
    /**
     * {@link XmlPushCallbackManager} that contains the {@link XmlPushCallback}
     * objects.
     */
    @Inject
    private XmlPushCallbackManager manager;

    /**
     * Gets the callback resource by its callbackId.
     *
     * @param callbackId
     *            callback ID assigned to the {@link XmlPushCallback} when it
     *            was registered
     * @return {@link XmlPushCallback} with the given callback ID, or
     *         {@code null} if one is not registered with that ID
     */
    @Path("/{id}")
    public XmlPushCallback getCallback(@PathParam("id") String callbackId)
    {
        return manager.getCallback(callbackId);
    }
}
