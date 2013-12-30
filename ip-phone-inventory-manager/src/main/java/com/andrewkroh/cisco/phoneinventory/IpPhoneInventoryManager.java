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

package com.andrewkroh.cisco.phoneinventory;

import com.google.common.collect.ImmutableSet;

/**
 * Manages the inventory of IP phones. The manager contains the hostname, MAC
 * address, credentials, and extensions associated with each phone.
 *
 * @author akroh
 */
public interface IpPhoneInventoryManager
{
    /**
     * Gets the set of all {@link IpPhone}s known to the manager.
     *
     * @return set of all {@code IpPhone} objects known to the manager
     */
    ImmutableSet<IpPhone> getIpPhones();
}
