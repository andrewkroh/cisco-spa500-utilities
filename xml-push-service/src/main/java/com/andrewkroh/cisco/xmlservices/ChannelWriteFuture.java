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

import com.google.common.util.concurrent.SettableFuture;

/**
 * Listens for the failure of {@link Channel#writeAndFlush(Object)} notifies
 * the {@link SettableFuture}.
 *
 * @author akroh
 */
class ChannelWriteFuture<T> implements ChannelFutureListener
{
    /**
     * SLF4J logger for this class.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ChannelWriteFuture.class);

    private final SettableFuture<T> responseFuture;

    /**
     * Constructs a new {@link ChannelFutureListener} that notifies the
     * specified {@link SettableFuture} if there is a failure.
     *
     * @param responseFuture
     *            SettableFuture to notify upon failure
     */
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