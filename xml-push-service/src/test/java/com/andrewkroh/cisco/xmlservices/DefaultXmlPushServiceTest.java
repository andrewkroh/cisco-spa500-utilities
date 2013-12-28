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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.NetUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.andrewkroh.cisco.common.TestUtils;
import com.andrewkroh.cisco.phoneinventory.IpPhone;
import com.cisco.xmlservices.XmlMarshaller;
import com.cisco.xmlservices.generated.CiscoIPPhoneExecute;
import com.cisco.xmlservices.generated.CiscoIPPhoneExecuteItemType;
import com.cisco.xmlservices.generated.CiscoIPPhoneResponse;
import com.cisco.xmlservices.generated.CiscoIPPhoneResponseItemType;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Test for {@link DefaultXmlPushService}.
 *
 * @author akroh
 */
public class DefaultXmlPushServiceTest
{
    /**
     * A test server for simulated a real Cisco IP phone.
     */
    private static class TestServer
    {
        private final EventLoopGroup bossGroup = new NioEventLoopGroup();

        private final EventLoopGroup workerGroup = new NioEventLoopGroup();

        private final HttpTestServerHandler testServerHandler =
                new HttpTestServerHandler();

        private final ServerBootstrap bootstrap;

        private final Channel channel;

        public TestServer() throws InterruptedException
        {
            bootstrap = new ServerBootstrap()
            .group(bossGroup, workerGroup)
            .localAddress(new InetSocketAddress(NetUtil.LOCALHOST,
                                                TestUtils.getFreePort()))
            .channel(NioServerSocketChannel.class)
            .childHandler(new HttpTestServerInitializer(testServerHandler));

            channel = bootstrap.bind().sync().channel();
        }

        public void shutdown()
        {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            channel.close();
        }

        public Channel getChannel()
        {
            return channel;
        }

        public void setResponse(Object ciscoJaxbObject)
        {
            testServerHandler.setResponse(ciscoJaxbObject);
        }

        public ListenableFuture<CiscoIPPhoneExecute> getCiscoIPPhoneExecute()
        {
            Function<String, CiscoIPPhoneExecute> transformFunc =
              new Function<String, CiscoIPPhoneExecute>() {
                public CiscoIPPhoneExecute apply(String xmlContent)
                {
                    return XmlMarshaller.unmarshal(
                            xmlContent,
                            CiscoIPPhoneExecute.class);
                }
            };
            return Futures.transform(testServerHandler.getReceivedHttpRequest(),
                                     transformFunc);
        }
    }

    private static final String TEST_URL = "http://localhost/phones/rest";

    private static final String CALLBACK_URL_TEMPLATE = "$BASEURL/get";

    private static final String BASE_CALLBACK_URL =
            "http://localhost/phones/rest/callback/12345";

    @Rule
    public final Timeout globalTimeout = new Timeout(10000);

    private final TestServer testServer;

    private final DefaultXmlPushService pushService;

    private final XmlPushCallback callback = mock(XmlPushCallback.class);

    private final XmlPushCallbackManager callbackManager =
            mock(XmlPushCallbackManager.class);

    public DefaultXmlPushServiceTest()
            throws InterruptedException, TimeoutException
    {
        testServer = new TestServer();
        pushService = new DefaultXmlPushService(callbackManager, 1000, 1000);
        pushService.startAsync().awaitRunning(10, TimeUnit.SECONDS);

        when(callbackManager.registerCallback(any(XmlPushCallback.class))).
            thenReturn(BASE_CALLBACK_URL);
    }

    @After
    public void cleanup() throws TimeoutException
    {
        pushService.stopAsync().awaitTerminated(10, TimeUnit.SECONDS);
        testServer.shutdown();
    }

    @Test
    public void submitCommand_returnedFutureContainsResponse()
            throws InterruptedException, ExecutionException, TimeoutException
    {
        // Configure response from test server:
        final CiscoIPPhoneResponse serverResponse =
                buildCiscoIPPhoneResponse(5, TEST_URL);
        testServer.setResponse(serverResponse);

        // Send request to server:
        final IpPhone phone = mockPhone(testServer);
        ListenableFuture<XmlPushResponse> receivedFuture =
                pushService.submitCommand(phone,
                                          buildCiscoIPPhoneExecute(TEST_URL));

        // Wait for server response:
        XmlPushResponse receivedResponse = receivedFuture.get();

        // Validate response:
        assertThat(receivedResponse, notNullValue());
        assertThat(receivedResponse.getPhone(), equalTo(phone));
        assertResponseEquals(serverResponse, receivedResponse.getResponse());
    }

    @Test
    public void submitCommand_withCallback_receivedUrlContainsCallbackId()
            throws InterruptedException, ExecutionException, TimeoutException
    {
        // Configure dummy response:
        final CiscoIPPhoneResponse serverResponse =
                buildCiscoIPPhoneResponse(5, TEST_URL);
        testServer.setResponse(serverResponse);

        // Send request to server:
        pushService.submitCommand(mockPhone(testServer),
                buildCiscoIPPhoneExecute(CALLBACK_URL_TEMPLATE), callback);

        verify(callbackManager).registerCallback(callback);

        // Verify that the callback ID was substituted:
        CiscoIPPhoneExecute command = testServer.getCiscoIPPhoneExecute().get();
        assertThat(command.getExecuteItem().get(0).getURL(),
                   startsWith(BASE_CALLBACK_URL));
    }

    @Test
    public void submitCommand_toBadAddress_returnedFutureContainsException()
            throws InterruptedException, TimeoutException
    {
        final IpPhone phone = mockPhoneWithWrongPort();
        ListenableFuture<XmlPushResponse> receivedFuture =
                pushService.submitCommand(phone,
                                          buildCiscoIPPhoneExecute(TEST_URL));

        try
        {
            receivedFuture.get();
            fail("Expected ExecutionException");
        }
        catch (ExecutionException e)
        {
            assertThat(e.getCause(), instanceOf(IOException.class));
        }
    }

    @Test(expected = CancellationException.class)
    public void submitCommon_returnedFutureTimesoutWithException()
            throws InterruptedException, TimeoutException, ExecutionException
    {
        final IpPhone phone = mockPhone(testServer);
        ListenableFuture<XmlPushResponse> receivedFuture =
                pushService.submitCommand(phone,
                                          buildCiscoIPPhoneExecute(TEST_URL));

        receivedFuture.get();
    }

    @Test
    public void submitCommands_returnedFuturesContainsResponse()
            throws InterruptedException, ExecutionException, TimeoutException
    {
        // Configure response from test server:
        final CiscoIPPhoneResponse serverResponse =
                buildCiscoIPPhoneResponse(5, TEST_URL);
        testServer.setResponse(serverResponse);
        final IpPhone phone = mockPhone(testServer);

        // Add a second server for this test:
        TestServer secondServer = new TestServer();
        secondServer.setResponse(serverResponse);
        final IpPhone secondPhone = mockPhone(secondServer);

        // Send request to server:
        ImmutableList<ListenableFuture<XmlPushResponse>> receivedFutures =
                pushService.submitCommand(ImmutableList.of(phone, secondPhone),
                                          buildCiscoIPPhoneExecute(TEST_URL));

        assertThat(receivedFutures, hasSize(2));

        // Validate first response:
        XmlPushResponse receivedResponse = receivedFutures.get(0).get();
        assertThat(receivedResponse, notNullValue());
        assertThat(receivedResponse.getPhone(), equalTo(phone));
        assertResponseEquals(serverResponse, receivedResponse.getResponse());

        // Validate second response:
        receivedResponse = receivedFutures.get(1).get();
        assertThat(receivedResponse, notNullValue());
        assertThat(receivedResponse.getPhone(), equalTo(secondPhone));
        assertResponseEquals(serverResponse, receivedResponse.getResponse());

        secondServer.shutdown();
    }

    @Test
    public void unregisterCallback_invokesCallbackManager()
    {
        pushService.unregisterCallback(callback);
        verify(callbackManager).unregisterCallback(callback);
    }

    private IpPhone mockPhoneWithWrongPort()
    {
        IpPhone phone = mock(IpPhone.class);
        when(phone.getUsername()).thenReturn("admin");
        when(phone.getPassword()).thenReturn("pass");
        when(phone.getHostname()).thenReturn(NetUtil.LOCALHOST.getHostAddress());
        when(phone.getPort()).thenReturn(TestUtils.getFreePort());
        return phone;
    }

    private IpPhone mockPhone(TestServer toServer)
    {
        IpPhone mockPhone = mock(IpPhone.class);
        when(mockPhone.getUsername()).thenReturn("admin");
        when(mockPhone.getPassword()).thenReturn("pass");

        if (toServer.getChannel().localAddress() instanceof InetSocketAddress)
        {
            InetSocketAddress address =
                    (InetSocketAddress) toServer.getChannel().localAddress();
            when(mockPhone.getHostname()).thenReturn(
                    address.getAddress().getHostAddress());
            when(mockPhone.getPort()).thenReturn(
                    address.getPort());
        }
        else
        {
            throw new IllegalArgumentException(
                    "SocketAddress is not a InetSocketAddress.");
        }

        return mockPhone;
    }

    private CiscoIPPhoneExecute buildCiscoIPPhoneExecute(String url)
    {
        CiscoIPPhoneExecute executeJaxbObject =
                new CiscoIPPhoneExecute();
        CiscoIPPhoneExecuteItemType executeItem =
                new CiscoIPPhoneExecuteItemType();
        executeItem.setPriority((short) 0);
        executeItem.setURL(url);
        executeJaxbObject.getExecuteItem().add(executeItem);
        return executeJaxbObject;
    }

    private CiscoIPPhoneResponse buildCiscoIPPhoneResponse(int status, String url)
    {
        CiscoIPPhoneResponse response = new CiscoIPPhoneResponse();
        CiscoIPPhoneResponseItemType responseItem =
                new CiscoIPPhoneResponseItemType();
        responseItem.setStatus((short) status);
        responseItem.setData("");
        responseItem.setURL(url);
        response.getResponseItem().add(responseItem);
        return response;
    }

    private void assertResponseEquals(CiscoIPPhoneResponse expected,
            CiscoIPPhoneResponse observed)
    {
        assertNotNull(expected);
        assertNotNull(observed);

        assertEquals(expected.getResponseItem().size(), observed.getResponseItem().size());
        for (int i = 0; i < expected.getResponseItem().size(); i++)
        {
            assertResponseItemEqual(expected.getResponseItem().get(i),
                      observed.getResponseItem().get(i));
        }
    }

    private void assertResponseItemEqual(CiscoIPPhoneResponseItemType expected,
                                         CiscoIPPhoneResponseItemType observed)
    {
        assertThat(expected.getData(), equalTo(observed.getData()));
        assertThat(expected.getStatus(), equalTo(observed.getStatus()));
        assertThat(expected.getURL(), equalTo(observed.getURL()));
    }
}
