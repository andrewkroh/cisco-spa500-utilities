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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

import javax.xml.bind.MarshalException;
import javax.xml.bind.UnmarshalException;

import org.junit.Test;

import com.cisco.xmlservices.generated.CiscoIPPhoneDirectoryEntryType;
import com.cisco.xmlservices.generated.CiscoIPPhoneExecute;
import com.cisco.xmlservices.generated.CiscoIPPhoneExecuteItemType;
import com.cisco.xmlservices.generated.CiscoIPPhoneResponse;

/**
 * Test for {@link XmlMarshaller}.
 *
 * @author akroh
 */
public class XmlMarshallerTest
{
    private static final String VALID_CISCO_EXECUTE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<CiscoIPPhoneExecute>" +
                "<ExecuteItem Priority=\"0\" URL=\"http://localhost/test.xml\"/>" +
            "</CiscoIPPhoneExecute>";

    private static final String INVALID_CISCO_EXECUTE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<CiscoIPPhoneExecute>" +
                "<ExecuteItem Priority=\"-1\" URL=\"http://localhost/test.xml\"/>" +
            "</CiscoIPPhoneExecute>";

    private static final String RESPONSE_FROM_PHONE =
            "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" +
            "<CiscoIPPhoneResponse>" +
              "<ResponseItem Status=\"0\" Data=\"\" URL=\"http://host/test.xml\"/>" +
              "<ResponseItem Status=\"0\" Data=\"\" URL=\"\"/>" +
              "<ResponseItem Status=\"0\" Data=\"\" URL=\"\"/>" +
            "</CiscoIPPhoneResponse>";

    private static final String URL = "http://localhost/test.xml";

    private static CiscoIPPhoneExecute buildCiscoIPPhoneExecute(
            int priority, String url)
    {
        CiscoIPPhoneExecute executeJaxbObject =
                new CiscoIPPhoneExecute();
        CiscoIPPhoneExecuteItemType executeItem =
                new CiscoIPPhoneExecuteItemType();
        executeItem.setPriority((short) priority);
        executeItem.setURL(url);
        executeJaxbObject.getExecuteItem().add(executeItem);
        return executeJaxbObject;
    }

    private final CiscoIPPhoneExecute executeJaxbObject =
            buildCiscoIPPhoneExecute(0, URL);

    @Test
    public void marshall_returnsXml()
    {
        String xml = XmlMarshaller.marshalToXml(executeJaxbObject);
        assertThat(xml, containsString("<CiscoIPPhoneExecute>"));
        assertThat(xml, containsString(URL));
        assertThat(xml, containsString("</CiscoIPPhoneExecute>"));
        assertThat(xml, not(containsString("\n")));
        System.out.println(xml);
    }

    @Test
    public void marshall_returnsPrettyXml()
    {
        String xml = XmlMarshaller.marshalToXml(executeJaxbObject, true);
        assertThat(xml, containsString("\n"));
    }

    @Test
    public void marshall_invalidObject_throwsException()
    {
        try
        {
            XmlMarshaller.marshalToXml(buildCiscoIPPhoneExecute(-1, URL));
            fail("Expected UnmarshalException wrapped by RuntimeException.");
        }
        catch (RuntimeException e)
        {
            assertThat(e.getCause(), instanceOf(MarshalException.class));
        }
    }

    @Test
    public void unmarshall_returnsObject()
    {
        Object obj = XmlMarshaller.unmarshal(VALID_CISCO_EXECUTE);

        assertThat(obj, instanceOf(CiscoIPPhoneExecute.class));
    }

    @Test
    public void unmarshall_returnsCiscoIPPhoneExecute()
    {
        CiscoIPPhoneExecute execute = XmlMarshaller.unmarshal(
                VALID_CISCO_EXECUTE, CiscoIPPhoneExecute.class);

        assertThat(execute, notNullValue());
        assertThat(execute.getExecuteItem().get(0), notNullValue());
        assertThat(execute.getExecuteItem().get(0).getPriority(), equalTo((short) 0));
        assertThat(execute.getExecuteItem().get(0).getURL(), equalTo(URL));
    }

    @Test
    public void unmarshall_invalidXml_throwsException()
    {
        try
        {
            XmlMarshaller.unmarshal(INVALID_CISCO_EXECUTE);
            fail("Expected UnmarshalException wrapped by RuntimeException.");
        }
        catch (RuntimeException e)
        {
            assertThat(e.getCause(), instanceOf(UnmarshalException.class));
        }
    }

    @Test(expected = ClassCastException.class)
    public void unmarshall_wrongClassType_throwsException()
    {
        XmlMarshaller.unmarshal(VALID_CISCO_EXECUTE,
                                 CiscoIPPhoneDirectoryEntryType.class);
    }

    public void unmarshall_ciscoIPPhoneResponse()
    {
        XmlMarshaller.unmarshal(RESPONSE_FROM_PHONE,
                                CiscoIPPhoneResponse.class);
    }
}
