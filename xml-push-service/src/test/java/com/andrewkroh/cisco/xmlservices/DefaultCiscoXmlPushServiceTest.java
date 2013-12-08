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

package com.andrewkroh.cisco.xmlservices;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;


import com.andrewkroh.cisco.xmlservices.DefaultCiscoXmlPushService;
import com.cisco.xmlservices.generated.CiscoIPPhoneExecute;
import com.cisco.xmlservices.generated.CiscoIPPhoneExecuteItemType;
import com.cisco.xmlservices.generated.CiscoIPPhoneResponse;

/**
 *
 * @author akroh
 */
public class DefaultCiscoXmlPushServiceTest
{

    @Test
    public void sendMessageToPhone() throws InterruptedException,
        ExecutionException, TimeoutException
    {
        CiscoIpPhone mockPhone = mock(CiscoIpPhone.class);
        when(mockPhone.getHostname()).thenReturn("spa502g-ext103.voice.va.crowbird.com");
        when(mockPhone.getPort()).thenReturn(80);
        when(mockPhone.getUsername()).thenReturn("admin");
        when(mockPhone.getPassword()).thenReturn("103");

        CiscoIPPhoneExecute execute = new CiscoIPPhoneExecute();
        CiscoIPPhoneExecuteItemType executeItem = new CiscoIPPhoneExecuteItemType();
        executeItem.setPriority((short) 0);
        executeItem.setURL("http://va.crowbird.com:28080/phones/tx_unicast.php");
        execute.getExecuteItem().add(executeItem);

        CiscoIpPhone mockPhone104 = mock(CiscoIpPhone.class);
        when(mockPhone104.getHostname()).thenReturn("spa502g-ext104.voice.va.crowbird.com");
        when(mockPhone104.getPort()).thenReturn(80);
        when(mockPhone104.getUsername()).thenReturn("admin");
        when(mockPhone104.getPassword()).thenReturn("104");

        CiscoIPPhoneExecute execute104 = new CiscoIPPhoneExecute();
        executeItem = new CiscoIPPhoneExecuteItemType();
        executeItem.setPriority((short) 0);
        executeItem.setURL("http://va.crowbird.com:28080/phones/tx_unicast.php");
        execute104.getExecuteItem().add(executeItem);

        DefaultCiscoXmlPushService service = new DefaultCiscoXmlPushService();
        service.startAsync().awaitRunning();
        System.out.println("Running.");
        Future<CiscoIPPhoneResponse> responseFuture =
                service.submitCommand(mockPhone, execute);
        Future<CiscoIPPhoneResponse> responseFuture104 =
                service.submitCommand(mockPhone104, execute104);
        assertNotNull(responseFuture.get(20, TimeUnit.SECONDS));
        assertNotNull(responseFuture104.get(20, TimeUnit.SECONDS));
    }
}
