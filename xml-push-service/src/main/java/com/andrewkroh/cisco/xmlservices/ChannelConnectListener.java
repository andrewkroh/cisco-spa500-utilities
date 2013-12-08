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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AttributeKey;

import com.google.common.util.concurrent.SettableFuture;

/**
 * Listens for successful client channel connection and then writes the
 * {@link HttpRequest} to the channel.
 *
 * @author akroh
 */
class ChannelConnectListener<T> implements ChannelFutureListener
{
    /**
     * SLF4J logger for this class.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ChannelConnectListener.class);

    /**
     * HTTP request to write to the channel after it is connected.
     */
    private final HttpRequest httpRequest;

    /**
     * {@code AttributeKey} used to put the {@code CiscoIpPhone} object into the
     * {@code Channel}.
     */
    private final AttributeKey<CiscoIpPhone> phoneAttributeKey;

    /**
     * {@code CiscoIpPhone} to store into the {@code Channel}.
     */
    private final CiscoIpPhone phone;

    /**
     * {@code AttributeKey} used to put the {@link SettableFuture} object into
     * the {@code Channel} using {@link Channel#attr(AttributeKey)}. The
     * {@code SettableFuture} is used to return the response from the
     * asynchronous call.
     */
    private final AttributeKey<SettableFuture<T>> responseFutureAttributeKey;

    /**
     * {@link SettableFuture} that is used to return the asynchronous response
     * to the caller or to indicate that an exception occurred. The client can
     * cancel the request, therefore this class should check if the
     * {@code Future} has been cancelled when it has the opportunity to
     * terminate the operation.
     */
    private final SettableFuture<T> responseFuture;

    /**
     * Constructs a new {@code ChannelFutureListener} that should be added as a
     * listener to the {@link Channel#connect(java.net.SocketAddress)}
     * operation. When that operation completes this class's
     * {@link #operationComplete(ChannelFuture)} is invoked.
     *
     * <p/>
     * Upon completion of the {@code connect} this will submit the HTTP request
     * using {@link Channel#writeAndFlush(Object)}.
     *
     * @param httpRequest
     *            {@code HttpRequest} to write to the channel after the channel
     *            is connected
     * @param phoneAttributeKey
     *            {@code AttributeKey} used to store the {@code CiscoIpPhone}
     *            with the channel
     * @param phone
     *            {@code CiscoIpPhone} to store with the channel
     * @param responseFutureAttributeKey
     *            {@code AttributeKey} used to store the {@code SettableFuture}
     *            with the channel
     * @param responseFuture
     *            {@code SettableFuture} to store with the channel
     */
    public ChannelConnectListener(HttpRequest httpRequest,
                                  AttributeKey<CiscoIpPhone> phoneAttributeKey,
                                  CiscoIpPhone phone,
                                  AttributeKey<SettableFuture<T>> responseFutureAttributeKey,
                                  SettableFuture<T> responseFuture)
    {
        this.httpRequest = httpRequest;
        this.phoneAttributeKey = phoneAttributeKey;
        this.phone = phone;
        this.responseFutureAttributeKey = responseFutureAttributeKey;
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
            channel.attr(phoneAttributeKey).set(phone);
            channel.attr(responseFutureAttributeKey).set(responseFuture);

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