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
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.xmlservices.XmlMarshaller;
import com.cisco.xmlservices.generated.CiscoIPPhoneExecute;
import com.cisco.xmlservices.generated.CiscoIPPhoneResponse;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.SettableFuture;

/**
 *
 * @author akroh
 */
public class DefaultCiscoXmlPushService extends AbstractService
    implements CiscoXmlPushService
{
    private static final Logger LOGGER =
            LoggerFactory.getLogger(DefaultCiscoXmlPushService.class);

    private static final AttributeKey<SettableFuture<CiscoIPPhoneResponse>> RESPONSE_KEY =
            AttributeKey.valueOf("RESPONSE_FUTURE");

    private static final String CISCO_CGI_EXECUTE_PATH = "/CGI/Execute";

    private static final Charset ENCODING = Charsets.UTF_8;

    private Bootstrap bootstrap;

    @Override
    protected void doStart()
    {
        bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                 .channel(NioSocketChannel.class)
                 .handler(new HttpClientInitializer(
                             new HttpSnoopClientHandler<CiscoIPPhoneResponse>(RESPONSE_KEY)))
                 .validate();

        notifyStarted();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doStop()
    {
        bootstrap.group().shutdownGracefully().addListener(
                new ShutdownListener());
    }

    @Override
    public Future<CiscoIPPhoneResponse> submitCommand(CiscoIpPhone phone,
            CiscoIPPhoneExecute command)
    {
        checkRunning();

        final FullHttpRequest httpRequest = buildRequest(phone.getHostname(),
                phone.getUsername(), phone.getPassword(), command);

        final SettableFuture<CiscoIPPhoneResponse> responseFuture =
                SettableFuture.create();

        ChannelFuture channelFuture = bootstrap.connect(phone.getHostname(),
                                                        phone.getPort());
        channelFuture.addListener(
                new ChannelConnectListener<CiscoIPPhoneResponse>(
                        httpRequest, RESPONSE_KEY, responseFuture));

        return responseFuture;
    }

    @Override
    public List<Future<CiscoIPPhoneResponse>> submitCommand(
            List<CiscoIpPhone> phones, CiscoIPPhoneExecute command)
    {
        checkRunning();

        // TODO: auto-generated stub
        throw new UnsupportedOperationException();
    }

    private void checkRunning()
    {
        Preconditions.checkArgument(isRunning(),
                "The service is not runnning.");
    }

    private FullHttpRequest buildRequest(String hostname, String username, String password,
            CiscoIPPhoneExecute command)
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
                postContent.toString().getBytes(Charsets.UTF_8);
        ByteBuf postContentByteBuf = Unpooled.wrappedBuffer(encodedPostContent);

        // Encode credentials:
        String encodedCredentials = base64EncodeCredentials(username, password);

        // Build the HTTP request:
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.POST,
                CISCO_CGI_EXECUTE_PATH, postContentByteBuf);
        request.headers().set(HttpHeaders.Names.HOST, hostname);
        request.headers().set(HttpHeaders.Names.AUTHORIZATION, encodedCredentials);
        request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
        request.headers().set(HttpHeaders.Names.CONTENT_TYPE, HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED);

        return request;
    }

    private static String base64EncodeCredentials(String username, String password)
    {
        StringBuilder credentials = new StringBuilder();
        credentials.append(username);
        credentials.append(':');
        credentials.append(password);

        return Base64.encodeBase64String(
                credentials.toString().getBytes(ENCODING));
    }

    private static String urlEncode(String source)
    {
        try
        {
            return URLEncoder.encode(source, ENCODING.name());
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("Unable to find UTF-8 charset.", e);
        }
    }

    private class ShutdownListener<T> implements
            GenericFutureListener<io.netty.util.concurrent.Future<T>>
    {
        @Override
        public void operationComplete(io.netty.util.concurrent.Future<T> future)
                throws Exception
        {
            notifyStopped();
        }
    }

    private static class ChannelConnectListener<T> implements ChannelFutureListener
    {
        private final HttpRequest httpRequest;

        private final AttributeKey<SettableFuture<T>> attributeKey;

        private final SettableFuture<T> responseFuture;



        public ChannelConnectListener(HttpRequest httpRequest,
                                      AttributeKey<SettableFuture<T>> attributeKey,
                                      SettableFuture<T> responseFuture)
        {
            this.httpRequest = httpRequest;
            this.attributeKey = attributeKey;
            this.responseFuture = responseFuture;
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception
        {
            LOGGER.debug("connect() complete, status: " + future);

            if (responseFuture.isCancelled())
            {
                future.channel().close();
                return;
            }

            if (future.isSuccess())
            {
                Channel channel = future.channel();
                channel.attr(attributeKey).set(responseFuture);

                channel.writeAndFlush(httpRequest).addListener(
                        new ChannelWriteFuture<T>(responseFuture));
            }
            else
            {
                responseFuture.setException(future.cause());
                future.channel().close();
            }
        }
    }

    private static class ChannelWriteFuture<T> implements ChannelFutureListener
    {
        private final SettableFuture<T> responseFuture;

        public ChannelWriteFuture(SettableFuture<T> responseFuture)
        {
            this.responseFuture = responseFuture;
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception
        {
            LOGGER.debug("write() complete, status: " + future);

            if (!future.isSuccess())
            {
                responseFuture.setException(future.cause());
                future.channel().close();
            }
        }

    }
}
