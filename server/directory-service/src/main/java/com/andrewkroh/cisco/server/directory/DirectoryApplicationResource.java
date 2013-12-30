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

package com.andrewkroh.cisco.server.directory;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.andrewkroh.cisco.server.phone.rest.PhoneApplication;
import com.cisco.xmlservices.generated.CiscoIPPhoneDirectory;
import com.cisco.xmlservices.generated.CiscoIPPhoneDirectoryEntryType;

/**
 * JAX-RS resource that responds with the XML directory.
 *
 * <p/>
 * It implements {@link PhoneApplication} which allows the class to be
 * automatically picked up and deployed by the phone-rest-service.
 *
 * @author akroh
 */
public class DirectoryApplicationResource implements PhoneApplication
{
    private final DirectoryManager directoryManager;

    @Inject
    public DirectoryApplicationResource(DirectoryManager directoryManager)
    {
        this.directoryManager = checkNotNull(directoryManager);
    }

    @Override
    public String getApplicationId()
    {
        return "directory";
    }

    @GET
    @Produces("application/xml")
    public CiscoIPPhoneDirectory getCiscoDirectory()
    {
        Directory directory = directoryManager.getDirectory();

        CiscoIPPhoneDirectory ciscoDir = new CiscoIPPhoneDirectory();
        ciscoDir.setTitle(directory.getTitle());

        for (DirectoryEntry entry : directory.getDirectoryEntries())
        {
            CiscoIPPhoneDirectoryEntryType ciscoDirEntry =
                    new CiscoIPPhoneDirectoryEntryType();
            ciscoDirEntry.setName(entry.getName());
            ciscoDirEntry.setTelephone(entry.getTelephoneNumber());
            ciscoDir.getDirectoryEntry().add(ciscoDirEntry);
        }

        return ciscoDir;
    }

    @GET
    @Path("/get")
    @Produces("application/json")
    public Directory getDirectory()
    {
        return directoryManager.getDirectory();
    }
}
