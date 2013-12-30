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

import java.io.IOException;
import java.net.URL;

import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.andrewkroh.cisco.server.directory.jaxb.JaxbDirectory;
import com.andrewkroh.cisco.server.directory.jaxb.JaxbDirectoryEntry;
import com.google.common.collect.ImmutableList;

/**
 * {@link DirectoryManager} that gets its data from an XML file.
 *
 * @author akroh
 */
@Default
public class XmlDirectoryManager implements DirectoryManager
{
    /**
     * Directory that was created from data contained in an XML file.
     */
    private final Directory directory;

    /**
     * Creates a new {@code XmlDirectoryManager} using the XML file located at
     * the given URL.
     *
     * @param directoryXml
     *            URL to the XML file containing the directory
     * @throws JAXBException
     *             if there is a problem parsing the XML
     * @throws IOException
     *             if there is a problem reading the XML file
     */
    @Inject
    public XmlDirectoryManager(@Named("directoryXml") URL directoryXml)
            throws JAXBException, IOException
    {
        checkNotNull(directoryXml, "URL to XML directory cannot be null.");
        JaxbDirectory jaxbDirectory = parseXmlFile(directoryXml);
        directory = convertJaxbDirectory(jaxbDirectory);
    }

    /**
     * Parses the file located at the given URL into a {@link JaxbDirectory}
     * object.
     *
     * @param directoryXml
     *            URL to the XML file containing the directory
     * @return {@code JaxbDirectory} object containing data read from the XML
     *         file
     * @throws JAXBException
     *             if there is a problem parsing the XML
     * @throws IOException
     *             if there is a problem reading the XML file
     */
    private static JaxbDirectory parseXmlFile(URL directoryXml)
            throws JAXBException, IOException
    {
        JAXBContext jaxbContext = JAXBContext.newInstance(JaxbDirectory.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        JaxbDirectory jaxbDirectory = (JaxbDirectory) jaxbUnmarshaller.unmarshal(
                directoryXml.openStream());
        return jaxbDirectory;
    }

    /**
     * Converts a {@link JaxbDirectory} object into a {@link Directory} object.
     *
     * @param jaxbDirectory
     *            {@code JaxbDirectory} used as the source in the conversion
     *
     * @return {@code Directory} created from the data in {@code jaxbDirectory}
     */
    private static Directory convertJaxbDirectory(JaxbDirectory jaxbDirectory)
    {
        ImmutableList.Builder<DirectoryEntry> entriesBuilder =
                ImmutableList.builder();

        for (JaxbDirectoryEntry jaxbEntry : jaxbDirectory.getDirectoryEntries())
        {
            entriesBuilder.add(new BasicDirectoryEntry(
                    jaxbEntry.getName(),
                    jaxbEntry.getTelephoneNumber()));
        }

        return new BasicDirectory(jaxbDirectory.getTitle(),
                                  entriesBuilder.build());
    }

    @Override
    public Directory getDirectory()
    {
        return directory;
    }
}
