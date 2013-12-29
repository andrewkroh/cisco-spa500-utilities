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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.URL;

import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.google.common.collect.ImmutableSet;

@Default
public class XmlIpPhoneInventoryManager implements IpPhoneInventoryManager
{
    private final ImmutableSet<IpPhone> phones;

    @Inject
    public XmlIpPhoneInventoryManager(
            @Named("phoneInventoryXml") URL phoneInventoryXml)
            throws JAXBException, IOException
    {
        checkNotNull(phoneInventoryXml, "URL to XML phone inventory cannot be null.");
        this.phones = parseXmlFile(phoneInventoryXml);
    }

    private static ImmutableSet<IpPhone> parseXmlFile(URL configFile)
            throws JAXBException, IOException
    {
        JAXBContext jaxbContext = JAXBContext.newInstance(JaxbPhoneInventory.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        JaxbPhoneInventory inventory = (JaxbPhoneInventory) jaxbUnmarshaller.unmarshal(
                configFile.openStream());
        return ImmutableSet.<IpPhone>copyOf(inventory.getPhones());
    }

    @Override
    public ImmutableSet<IpPhone> getIpPhones()
    {
        return phones;
    }
}
