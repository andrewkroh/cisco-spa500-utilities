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

import java.io.IOException;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.junit.Test;

/**
 * Unit test for {@link XmlDirectoryManager}.
 *
 * @author akroh
 */
public class XmlDirectoryManagerTest
{
    private static final URL TEST_XML =
            XmlDirectoryManagerTest.class.getResource("/test-directory.xml");

    @Test(expected = NullPointerException.class)
    public void constructor_withNullUrl_throwsException()
            throws JAXBException, IOException
    {
        new XmlDirectoryManager(null);
    }

    @Test
    public void constructor_readsXml() throws JAXBException, IOException
    {
        XmlDirectoryManager manager = new XmlDirectoryManager(TEST_XML);
        Directory dir = manager.getDirectory();

        assertThat(dir.getTitle(), equalTo("My Title"));
        assertThat(dir.getDirectoryEntries(), hasSize(2));
        assertThat(dir.getDirectoryEntries().get(0).getName(),
                equalTo("John Smith"));
        assertThat(dir.getDirectoryEntries().get(0).getTelephoneNumber(),
                equalTo("555-555-0001"));
        assertThat(dir.getDirectoryEntries().get(1).getName(),
                equalTo("Jane Smith"));
        assertThat(dir.getDirectoryEntries().get(1).getTelephoneNumber(),
                equalTo("555-555-0002"));
    }
}
