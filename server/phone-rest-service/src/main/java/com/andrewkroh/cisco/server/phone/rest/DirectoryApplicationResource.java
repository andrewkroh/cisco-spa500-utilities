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

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import com.cisco.xmlservices.generated.CiscoIPPhoneDirectory;
import com.cisco.xmlservices.generated.CiscoIPPhoneDirectoryEntryType;

/**
 *
 * @author akroh
 */
public class DirectoryApplicationResource implements PhoneApplication
{
    @GET
    @Produces("application/xml")
    public CiscoIPPhoneDirectory getDirectory()
    {
        CiscoIPPhoneDirectory dir = new CiscoIPPhoneDirectory();
        dir.setTitle("Kroh Title");
        CiscoIPPhoneDirectoryEntryType entry =
                new CiscoIPPhoneDirectoryEntryType();
        entry.setName("Andrew");
        entry.setTelephone("814-265-8285");
        dir.getDirectoryEntry().add(entry);

        return dir;
    }

    @Override
    public String getApplicationId()
    {
        return "directory";
    }
}
