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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Directory class that can be written to and read from XML using JAXB.
 *
 * @author akroh
 */
@XmlRootElement(name = "Directory")
public class JaxbDirectory
{
    @XmlElement(required = true)
    private final String title;

    @XmlElement(name = "entry", nillable = false, required = true)
    private final List<JaxbDirectoryEntry> entries;

    public JaxbDirectory(String title,
                         List<JaxbDirectoryEntry> entries)
    {
        this.title = checkNotNull(title);
        this.entries = checkNotNull(entries);
    }

    /**
     * No-arg constructor that is required for JAXB.
     */
    @SuppressWarnings("unused")
    private JaxbDirectory()
    {
        this.title = null;
        this.entries = null;
    }

    public String getTitle()
    {
        return title;
    }

    public List<JaxbDirectoryEntry> getDirectoryEntries()
    {
        return entries;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                    .append("title", title)
                    .append("entries", entries)
                    .build();
    }
}
