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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.cisco.xmlservices.generated.CiscoIPPhoneDirectory;
import com.google.common.collect.ImmutableList;

/**
 * Unit test for {@link DirectoryApplicationResource}.
 *
 * @author akroh
 */
public class DirectoryApplicationResourceTest
{
    private final static String TITLE = "My Title";

    private final static String NAME = "My Name";

    private final static String NUMBER = "555-555-5555";

    /**
     * {@link DirectoryApplicationResource} under test.
     */
    private final DirectoryApplicationResource resource;

    public DirectoryApplicationResourceTest()
    {
        DirectoryEntry entry = mock(DirectoryEntry.class);
        when(entry.getName()).thenReturn(NAME);
        when(entry.getTelephoneNumber()).thenReturn(NUMBER);

        Directory directory = mock(Directory.class);
        when(directory.getTitle()).thenReturn(TITLE);
        when(directory.getDirectoryEntries()).thenReturn(ImmutableList.of(entry));

        DirectoryManager manager = mock(DirectoryManager.class);
        when(manager.getDirectory()).thenReturn(directory);

        resource = new DirectoryApplicationResource(manager);
    }

    /**
     * Tests that the constructor throws an exception when a null
     * {@link DirectoryManager} is passed in.
     */
    @Test(expected = NullPointerException.class)
    public void constructor_withNullManager_throwsException()
    {
        new DirectoryApplicationResource(null);
    }

    /**
     * Tests that {@code getDirectory} returns a {@link CiscoIPPhoneDirectory}
     * object translated from {@link Directory}.
     */
    @Test
    public void getDirectory_returnsCiscoDirectory()
    {
        CiscoIPPhoneDirectory dir = resource.getCiscoDirectory();
        assertThat(dir.getTitle(), equalTo(TITLE));
        assertThat(dir.getDirectoryEntry(), hasSize(1));
        assertThat(dir.getDirectoryEntry().get(0).getName(), equalTo(NAME));
        assertThat(dir.getDirectoryEntry().get(0).getTelephone(), equalTo(NUMBER));
    }
}
