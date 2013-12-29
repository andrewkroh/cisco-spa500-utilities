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

import javax.xml.bind.annotation.XmlElement;

/**
 * Basic immutable implementation of {@link DirectoryEntry}.
 *
 * @author akroh
 */
public class BasicDirectoryEntry implements DirectoryEntry
{
    @XmlElement(required = true)
    private final String name;

    @XmlElement(required = true)
    private final String telephoneNumber;

    public BasicDirectoryEntry(String name, String telephoneNumber)
    {
        this.name = checkNotNull(name);
        this.telephoneNumber = checkNotNull(telephoneNumber);
    }

    /**
     * No-arg constructor that is required for JAXB.
     */
    @SuppressWarnings("unused")
    private BasicDirectoryEntry()
    {
        this.name = null;
        this.telephoneNumber = null;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getTelephoneNumber()
    {
        return telephoneNumber;
    }
}
