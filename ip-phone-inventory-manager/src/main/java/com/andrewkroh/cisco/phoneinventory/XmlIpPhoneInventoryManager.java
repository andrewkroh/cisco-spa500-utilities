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

import com.andrewkroh.cisco.phoneinventory.jaxb.JaxbIpPhone;
import com.andrewkroh.cisco.phoneinventory.jaxb.JaxbPhoneInventory;
import com.google.common.collect.ImmutableSet;

/**
 * IP phone inventory manager that reads its data from an XML file.
 *
 * @author akroh
 */
@Default
public class XmlIpPhoneInventoryManager implements IpPhoneInventoryManager
{
    /**
     * Set of all {@link IpPhone}s read from the XML file.
     */
    private final ImmutableSet<IpPhone> phones;

    /**
     * Creates a new {@code XmlIpPhoneInventoryManager} and initializes it with
     * data read from the specified XML file.
     *
     * @param phoneInventoryXml
     *            URL to the XML file containing the IP phone inventory
     * @throws JAXBException
     *             if there is a problem parsing the XML
     * @throws IOException
     *             if there is a problem reading the XML file
     */
    @Inject
    public XmlIpPhoneInventoryManager(
            @Named("phoneInventoryXml") URL phoneInventoryXml)
            throws JAXBException, IOException
    {
        checkNotNull(phoneInventoryXml, "URL to XML phone inventory cannot be null.");
        this.phones = parseXmlFile(phoneInventoryXml);
    }

    /**
     * Parses the file located at the given URL into a set of {@link IpPhone}
     * objects.
     *
     * @param phoneInventoryXml
     *            URL to the XML file containing the IP phone inventory
     * @return set of {@link IpPhone} objects read from the XML file
     * @throws JAXBException
     *             if there is a problem parsing the XML
     * @throws IOException
     *             if there is a problem reading the XML file
     */
    private static ImmutableSet<IpPhone> parseXmlFile(URL phoneInventoryXml)
            throws JAXBException, IOException
    {
        JAXBContext jaxbContext = JAXBContext.newInstance(
                JaxbPhoneInventory.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        JaxbPhoneInventory jaxbInventory = (JaxbPhoneInventory) jaxbUnmarshaller.
                unmarshal(phoneInventoryXml.openStream());

        System.out.println(jaxbInventory);
        ImmutableSet.Builder<IpPhone> builder = ImmutableSet.builder();
        for (JaxbIpPhone jaxbPhone : jaxbInventory.getPhones())
        {
            builder.add(new BasicIpPhone(jaxbPhone.getHostname(),
                    jaxbPhone.getPort(), jaxbPhone.getUsername(),
                    jaxbPhone.getPassword()));
        }
        return builder.build();
    }

    @Override
    public ImmutableSet<IpPhone> getIpPhones()
    {
        return phones;
    }
}
