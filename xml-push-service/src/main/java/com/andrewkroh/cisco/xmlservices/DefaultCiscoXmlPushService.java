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

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.xmlservices.XmlMarshaller;
import com.cisco.xmlservices.generated.CiscoIPPhoneExecute;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.SettableFuture;

/**
 * Implementation of the {@link CiscoXmlPushService} that uses Netty under the
 * hood to issue the requests to phones.
 *
 * @author akroh
 */
public class DefaultCiscoXmlPushService extends AbstractService
    implements CiscoXmlPushService
{
    /**
     * SLF4J logger for this class.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(DefaultCiscoXmlPushService.class);

    /**
     * Default socket connect timeout.
     */
    private static final int DEFAULT_CONNECT_TIMEOUT_MS =
            (int) TimeUnit.SECONDS.toMillis(10);

    /**
     * Default timeout period for returned {@code Future}s.
     */
    private static final int DEFAULT_RESPONSE_TIMEOUT_MS =
            (int) TimeUnit.SECONDS.toMillis(20);
    /**
     * {@code AttributeKey} used to obtain the {@link CiscoIpPhone} object from
     * its associated {@code Channel} using {@link Channel#attr(AttributeKey)}.
     */
    private static final AttributeKey<CiscoIpPhone> PHONE_KEY =
            AttributeKey.valueOf("PHONE_KEY");

    /**
     * {@code AttributeKey} used to obtain the {@link SettableFuture} object
     * from its associated {@code Channel} using
     * {@link Channel#attr(AttributeKey)}. The {@code SettableFuture} is used to
     * return the {@link CiscoXmlPushResponse} from the asynchronous call.
     */
    private static final AttributeKey<SettableFuture<CiscoXmlPushResponse>> PUSH_RESP_KEY =
            AttributeKey.valueOf("PUSH_RESP_KEY");

    /**
     * The target URL of a phone push (HTTP POST) requests.
     */
    private static final String CISCO_CGI_EXECUTE_PATH = "/CGI/Execute";

    /**
     * Type used for encoding for the content in the HTTP POST request.
     */
    private static final Charset ENCODING = Charsets.UTF_8;

    private final long connectTimeoutMs;

    private final long responseTimeoutMs;

    /**
     * Netty {@link Bootstrap} used for client connections.
     */
    private Bootstrap bootstrap;

    public DefaultCiscoXmlPushService()
    {
        this(DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_RESPONSE_TIMEOUT_MS);
    }

    public DefaultCiscoXmlPushService(int connectTimeoutMs, int responseTimeoutMs)
    {
        this.connectTimeoutMs = connectTimeoutMs;
        this.responseTimeoutMs = responseTimeoutMs;
    }

    @Override
    protected void doStart()
    {
        ChannelInboundHandler handler = new CiscoXmlResponseChannelHandler(
                                                PHONE_KEY, PUSH_RESP_KEY);

        bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                 .channel(NioSocketChannel.class)
                 .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectTimeoutMs)
                 .handler(new HttpClientInitializer(handler))
                 .validate();

        notifyStarted();

        LOGGER.info("Service is now STARTED.");
    }

    // This is needed because EventExecutorGroup declares its
    // return type as Future<?>.
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected void doStop()
    {
        LOGGER.info("Service is now STOPPING.");

        bootstrap.group().shutdownGracefully().addListener(
                new ShutdownListener());
    }

    @Override
    public ListenableFuture<CiscoXmlPushResponse> submitCommand(
            CiscoIpPhone phone, CiscoIPPhoneExecute command)
    {
        checkRunning();

        final HttpRequest httpRequest = buildRequest(phone.getHostname(),
                phone.getUsername(), phone.getPassword(), command);

        final SettableFuture<CiscoXmlPushResponse> responseFuture =
                SettableFuture.create();

        ChannelFuture channelFuture = bootstrap.connect(phone.getHostname(),
                                                        phone.getPort());
        channelFuture.addListener(
                new ChannelConnectListener<CiscoXmlPushResponse>(
                        httpRequest,
                        PHONE_KEY, phone,
                        PUSH_RESP_KEY, responseFuture,
                        bootstrap.group(),
                        responseTimeoutMs));

        return responseFuture;
    }

    @Override
    public ImmutableList<ListenableFuture<CiscoXmlPushResponse>> submitCommand(
            ImmutableList<CiscoIpPhone> phones, CiscoIPPhoneExecute command)
    {
        Builder<ListenableFuture<CiscoXmlPushResponse>> listBuilder =
                ImmutableList.builder();

        for (CiscoIpPhone phone : phones)
        {
            listBuilder.add(submitCommand(phone, command));
        }

        return listBuilder.build();
    }

    /**
     * Asserts that the service is in the {@code RUNNING} state.
     *
     * @throws IllegalStateException
     *             if the service in not in the {@code RUNNING} state
     * @see Service#isRunning()
     */
    private void checkRunning()
    {
        Preconditions.checkState(isRunning(),
                "The service is not RUNNING.");
    }

    /**
     * Builds a HTTP POST request containing the marshaled CiscoIPPhoneExecute
     * object in the body.
     *
     * <p/>
     * The phones require authentication in order to execute the commands. The
     * specified {@code username} and {@code password} will be included in the
     * request using HTTP Basic Authentication.
     *
     * @param hostname
     *            hostname of the server where the request will be sent
     * @param username
     *            username for authentiation to the phone
     * @param password
     *            password for authentication to the phone
     * @param command
     *            command to execute, this will be marshaled to XML
     * @return a fully-populated HTTP request
     */
    private static HttpRequest buildRequest(String hostname, String username,
            String password, CiscoIPPhoneExecute command)
    {
        Preconditions.checkNotNull(username, "Username cannot be null.");
        Preconditions.checkNotNull(password, "Password cannot be null.");
        Preconditions.checkNotNull(command,
                "CiscoIPPhoneExecute object cannot be null.");

        // Marshal object to XML and escape the XML:
        String objectAsXml = XmlMarshaller.marshalToXml(command);
        String urlEncodedXml = urlEncode(objectAsXml);

        // Build the content of the POST body:
        StringBuilder postContent = new StringBuilder();
        postContent.append("XML=");
        postContent.append(urlEncodedXml);

        // Encode the POST body to bytes:
        byte[] encodedPostContent =
                postContent.toString().getBytes(ENCODING);
        ByteBuf postContentByteBuf = Unpooled.wrappedBuffer(encodedPostContent);

        // Encode credentials:
        String encodedCredentials = base64EncodeCredentials(username, password);

        // Build the HTTP request:
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.POST,
                CISCO_CGI_EXECUTE_PATH, postContentByteBuf);
        request.headers().set(HttpHeaders.Names.HOST, hostname);
        request.headers().set(HttpHeaders.Names.AUTHORIZATION,
                encodedCredentials);
        request.headers().set(HttpHeaders.Names.CONNECTION,
                HttpHeaders.Values.CLOSE);
        request.headers().set(HttpHeaders.Names.CONTENT_TYPE,
                HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED);

        return request;
    }

    /**
     * This method encodes the credentials for use in HTTP Basic Authentication.
     * The username and password are combined into a string "username:password".
     * That string is then encoded using base64.
     *
     * @param username
     *            username to encode
     * @param password
     *            password to encode
     * @return encoded {@code username} and {@code password} for use in HTTP
     *         Basic Authentication
     */
    private static String base64EncodeCredentials(String username, String password)
    {
        StringBuilder credentials = new StringBuilder();
        credentials.append(username);
        credentials.append(':');
        credentials.append(password);

        return Base64.encodeBase64String(
                credentials.toString().getBytes(ENCODING));
    }

    /**
     * Translates a string into <code>application/x-www-form-urlencoded</code>
     * format using a specific encoding scheme. This method uses UTF-8 to be
     * compliant with the <a href=
     * "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars"> World
     * Wide Web Consortium Recommendation</a> recommentation.
     *
     * @param s
     *            <code>String</code> to be translated.
     * @return the translated <code>String</code>.
     */
    private static String urlEncode(String source)
    {
        try
        {
            // Must use UTF-8 to be W3C complaint.
            return URLEncoder.encode(source, Charsets.UTF_8.name());
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("Unsupported charset (UTF-8).", e);
        }
    }

    /**
     * Shutdown listener for the {@link EventExecutorGroup} used by the
     * {@code bootstrap}.
     *
     * <p/>
     * Because {@link EventExecutorGroup#shutdownGracefully() returns {
     * @code Future<?>} there are type issues when trying to add a listener.
     */
    private class ShutdownListener<T> implements
            GenericFutureListener<io.netty.util.concurrent.Future<T>>
    {
        @Override
        public void operationComplete(io.netty.util.concurrent.Future<T> future)
                throws Exception
        {
            // Transition to service from STOPPING --> STOPPED.
            notifyStopped();

            LOGGER.info("Service is now STOPPED.");
        }
    }
}
