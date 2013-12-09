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

/**
 * Interface for accessing information about a Cisco IP phone.
 *
 * @author akroh
 */
public interface CiscoIpPhone
{
    /**
     * Gets the hostname (or IP) of the phone.
     *
     * @return hostname or IP address of the phone
     */
    String getHostname();

    /**
     * Gets the port used by the phone HTTP interface.
     *
     * @return the phones HTTP port
     */
    int getPort();

    /**
     * Gets the username for authenticating to the phone.
     *
     * @return the username for authenticating to the phone
     */
    String getUsername();

    /**
     * Gets the password for authenticating to the phone.
     *
     * @return the password for authenticating to the phone
     */
    String getPassword();
}
