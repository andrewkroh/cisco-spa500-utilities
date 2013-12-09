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

import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import com.google.common.base.Preconditions;

/**
 * Boilerplate {@link ChannelInitializer} for HTTP.
 *
 * @author akroh
 */
public class HttpClientInitializer extends ChannelInitializer<SocketChannel>
{
    /**
     * Final handler in the pipeline.
     */
    private final ChannelInboundHandler handler;

    public HttpClientInitializer(ChannelInboundHandler handler)
    {
        this.handler = Preconditions.checkNotNull(handler,
                "Inbound HTTP Handler cannot be null.");
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception
    {
        // Create a default pipeline implementation.
        ChannelPipeline p = ch.pipeline();

        // Create a logger for debugging purposes:
        p.addLast("log", new LoggingHandler(LogLevel.INFO));

        // Adds codec for handling HTTP messages:
        p.addLast("codec", new HttpClientCodec());

        // Add decompresser for GZIP'ed messages:
        p.addLast("inflater", new HttpContentDecompressor());

        // Add automatic handling of "chunked" HTTP messages:
        p.addLast("aggregator", new HttpObjectAggregator(1048576));

        // Finally add our handler:
        p.addLast("handler", handler);
    }
}
