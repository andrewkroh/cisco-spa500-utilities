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

import static com.google.common.base.Preconditions.*;

import com.google.common.collect.ImmutableList;

/**
 * Basic immutable implementation of {@link Directory}.
 *
 * @author akroh
 */
public class BasicDirectory implements Directory
{
    private final String title;

    private final ImmutableList<DirectoryEntry> entries;

    public BasicDirectory(String title, ImmutableList<DirectoryEntry> entries)
    {
        this.title = checkNotNull(title);
        this.entries = checkNotNull(entries);
    }

    @Override
    public String getTitle()
    {
        return title;
    }

    @Override
    public ImmutableList<DirectoryEntry> getDirectoryEntries()
    {
        return entries;
    }
}
