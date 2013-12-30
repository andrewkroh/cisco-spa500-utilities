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

import javax.validation.constraints.NotNull;

/**
 * Manages {@link XmlPushCallback}s that are available to receive callbacks from
 * IP phones. Once a callback has been registered additional calls to register
 * it will have no effect. A callback should be explicitly unregistered after it
 * is no longer in use.
 *
 * @author akroh
 */
public interface XmlPushCallbackManager
{
    /**
     * Gets a {@link XmlPushCallback} by its assigned callback ID.
     *
     * @param callbackId
     *            unique ID assigned to the {@code XmlPushCallback} to obtain
     * @return XmlPushCallback with assigned {@code callbackId}, or {@code null}
     *         if one is not registered with the given ID
     *
     * @throws NullPointerException
     *             if callbackId is {@code null}
     */
    XmlPushCallback getCallback(@NotNull String callbackId);

    /**
     * Registers a {@link XmlPushCallback} so that it can receive callbacks from
     * IP phones. Additional calls to register a resource that is already
     * registered will have no effect.
     *
     * @param commandCallback
     *            XmlPushCallback to register, cannot be {@code null}
     * @return URL where the XmlPushCallback has been published or was already
     *         published
     *
     * @throws NullPointerException
     *             if {@code commandCallback} is {@code null}
     */
    @NotNull
    String registerCallback(@NotNull XmlPushCallback commandCallback);

    /**
     * Unregisters a {@link XmlPushCallback}.
     *
     * @param commandCallback
     *            XmlPushCallback to unregister, {@code null} is safely handled.
     * @return true if {@code commandCallback} has been unregistered
     */
    boolean unregisterCallback(XmlPushCallback commandCallback);
}
