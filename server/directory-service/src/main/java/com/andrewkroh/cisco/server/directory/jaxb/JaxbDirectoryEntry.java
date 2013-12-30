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

package com.andrewkroh.cisco.server.directory.jaxb;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Directory entry that is contained within a {@link JaxbDirectory}.
 *
 * @author akroh
 */
public class JaxbDirectoryEntry
{
    @XmlElement(required = true)
    private final String name;

    @XmlElement(required = true)
    private final String telephoneNumber;

    public JaxbDirectoryEntry(String name, String telephoneNumber)
    {
        this.name = checkNotNull(name);
        this.telephoneNumber = checkNotNull(telephoneNumber);
    }

    /**
     * No-arg constructor that is required for JAXB.
     */
    @SuppressWarnings("unused")
    private JaxbDirectoryEntry()
    {
        this.name = null;
        this.telephoneNumber = null;
    }

    public String getName()
    {
        return name;
    }

    public String getTelephoneNumber()
    {
        return telephoneNumber;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                    .append("name", name)
                    .append("telephoneNumber", telephoneNumber)
                    .build();
    }
}
