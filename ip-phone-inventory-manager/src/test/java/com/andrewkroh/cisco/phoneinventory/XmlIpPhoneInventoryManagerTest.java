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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.io.IOException;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.junit.Test;

/**
 * Test for {@link XmlIpPhoneInventoryManager}.
 *
 * @author akroh
 */
public class XmlIpPhoneInventoryManagerTest
{
    private static final URL TEST_XML =
            XmlIpPhoneInventoryManagerTest.class.getResource("/test-inventory.xml");

    @Test(expected = NullPointerException.class)
    public void constructor_withNullUrl_throwsException()
            throws JAXBException, IOException
    {
        new XmlIpPhoneInventoryManager(null);
    }

    @Test
    public void constructor_readsXml()
            throws JAXBException, IOException
    {
        IpPhoneInventoryManager inventory =
                new XmlIpPhoneInventoryManager(TEST_XML);
        assertThat(inventory.getIpPhones(), hasSize(2));
        assertThat(inventory.getIpPhones().asList().get(0).getHostname(),
                equalTo("spa502g-ext1001.voice.local"));
        assertThat(inventory.getIpPhones().asList().get(1).getHostname(),
                equalTo("spa502g-ext1002.voice.local"));
        assertThat(inventory.getIpPhones().asList().get(0).getPort(),
                equalTo(80));
        assertThat(inventory.getIpPhones().asList().get(1).getPort(),
                equalTo(80));
        assertThat(inventory.getIpPhones().asList().get(0).getUsername(),
                equalTo("admin"));
        assertThat(inventory.getIpPhones().asList().get(1).getUsername(),
                equalTo("admin"));
        assertThat(inventory.getIpPhones().asList().get(0).getPassword(),
                equalTo("pass"));
        assertThat(inventory.getIpPhones().asList().get(1).getPassword(),
                equalTo("pass"));
    }
}
