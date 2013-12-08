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
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class HttpTestServerInitializer extends
        ChannelInitializer<SocketChannel>
{
    private final ChannelInboundHandler handler;

    public HttpTestServerInitializer(ChannelInboundHandler handler)
    {
        this.handler = handler;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception
    {
        ChannelPipeline p = ch.pipeline();
        p.addLast("decoder", new HttpRequestDecoder());
        p.addLast("aggregator", new HttpObjectAggregator(1048576));
        p.addLast("encoder", new HttpResponseEncoder());
        p.addLast("handler", handler);
    }
}
