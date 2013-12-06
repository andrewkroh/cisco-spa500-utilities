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

package com.cisco.xmlservices;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.cisco.xmlservices.generated.ObjectFactory;
import com.google.common.base.Preconditions;

/**
 * Utility class for marshalling and unmarshalling Cisco IP Phone objects
 * to and from XML.
 *
 * @author akroh
 */
public final class XmlMarshaller
{
    /**
     * Private constructor to prevent instantiation.
     */
    private XmlMarshaller() {}

    /**
     * {@link ValidationEventHandler} that stop the marshalling or unmarshalling
     * on any warning or error while processing the object or XML.
     */
    private static class SchemaValidationHandler
        implements ValidationEventHandler
    {
        @Override
        public boolean handleEvent(ValidationEvent event)
        {
            // Always stop processing on warning/error:
            return false;
        }
    }

    /**
     * Location of the schema file contained on the classpath.
     */
    private static final String SCHEMA_LOCATION = "/schema/CiscoIPPhone.xsd";

    /**
     * JAXB context for processing objects within the
     * {@code com.cisco.xmlservices.generated} package.
     */
    private static JAXBContext context;

    /**
     * In-memory representation of the Cisco IP Phone schema.
     */
    private static Schema schema;

    /**
     * Schema validation handler that always stops the processing
     * when any error or warning is encountered.
     */
    private static ValidationEventHandler validator =
            new SchemaValidationHandler();

    /**
     * Method which lazily initializes the JAXB context.
     */
    private static synchronized void initJaxb()
    {
        // Double-check locking using context:
        if (context == null)
        {
            try
            {
                context = JAXBContext.newInstance(
                        ObjectFactory.class.getPackage().getName());

                SchemaFactory factory = SchemaFactory.newInstance(
                        XMLConstants.W3C_XML_SCHEMA_NS_URI);
                schema = factory.newSchema(
                        XmlMarshaller.class.getResource(SCHEMA_LOCATION));
            }
            catch (JAXBException e)
            {
                throw new RuntimeException(e);
            }
            catch (SAXException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Marshals the given object to XML. The object must be from the
     * {@code com.cisco.xmlservices.generated} package. This method does not
     * format the generated XML (it delegates to
     * {@link #marshalToXml(Object, boolean)} with {@code pretty == false}).
     *
     * @param source
     *            source object to marshal
     * @return the object as XML in a String
     *
     * @throws RuntimeException
     *             if the object is not valid according to the schema or if the
     *             object is not from the
     *             {@code com.cisco.xmlservices.generated} package
     */
    public static String marshalToXml(Object source)
    {
        return marshalToXml(source, false);
    }

    /**
     * Marshals the given object to XML. The object must be from the
     * {@code com.cisco.xmlservices.generated} package.
     *
     * @param source
     *            source object to marshal
     * @param pretty
     *            boolean flag indicating if the output XML should before
     *            formatted with indentation and newlines
     * @return the object as XML in a String
     *
     * @throws RuntimeException
     *             if the object is not valid according to the schema or if the
     *             object is not from the
     *             {@code com.cisco.xmlservices.generated} package
     */
    public static String marshalToXml(Object source, boolean pretty)
    {
        Preconditions.checkNotNull(source,
                "Source object to marshal cannot be null.");

        if (context == null)
        {
            initJaxb();
        }

        try
        {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setSchema(schema);
            marshaller.setEventHandler(validator);

            if (pretty)
            {
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                                       Boolean.TRUE);
            }

            StringWriter sw = new StringWriter();
            marshaller.marshal(source, sw);
            return sw.toString();
        }
        catch (JAXBException e)
        {
            throw new RuntimeException(e);
        }

    }

    /**
     * Unmarshals the given XML string to its object representation and casts it
     * to the specified Class type.
     *
     * @param source
     *            XML to unmarshal
     * @param type
     *            {@code Class} type to cast the unmarshalled {@code Object} to
     * @return the Object representation of the given XML cast to the specified
     *         {@code type}
     *
     * @throws RuntimeException
     *             if the XML is not valid according to the schema or if the XML
     *             does not represent an object in the
     *             {@code com.cisco.xmlservices.generated} package
     * @throws ClassCastException
     *             if the specified {@code type} does not match the actual
     *             unmarshalled type
     */
    public static <T> T unmarshal(String source, Class<T> type)
    {
        Preconditions.checkNotNull(type,
                "Type to cast the unmarshalled object to cannot be null.");

        // Force the cast to happen here by calling Class<T>.cast():
        return type.cast(unmarshal(source));
    }

    /**
     * Unmarshals the given XML string to its object representation.
     *
     * @param source
     *            XML to unmarshal
     * @return the Object representation of the given XML
     *
     * @throws RuntimeException
     *             if the XML is not valid according to the schema or if the XML
     *             does not represent an object in the
     *             {@code com.cisco.xmlservices.generated} package
     */
    public static Object unmarshal(String source)
    {
        Preconditions.checkNotNull(source,
                "The source XML string to unmarshal cannot be null.");

        if (context == null)
        {
            initJaxb();
        }

        try
        {
            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setSchema(schema);
            unmarshaller.setEventHandler(validator);

            StringReader sr = new StringReader(source);
            return unmarshaller.unmarshal(sr);
        }
        catch (JAXBException e)
        {
            throw new RuntimeException(e);
        }
    }
}
