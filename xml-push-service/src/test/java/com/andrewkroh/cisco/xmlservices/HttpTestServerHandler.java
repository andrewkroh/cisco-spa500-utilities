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

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.codec.Charsets;

import com.cisco.xmlservices.XmlMarshaller;
import com.google.common.util.concurrent.Atomics;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

public class HttpTestServerHandler extends SimpleChannelInboundHandler<Object>
{
    private final SettableFuture<HttpRequest> httpRequestFuture =
            SettableFuture.create();

    private final AtomicReference<HttpResponse> httpResponseRef =
            Atomics.newReference();

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception
    {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg)
            throws Exception
    {
        if (msg instanceof HttpRequest)
        {
            HttpRequest httpRequest = (HttpRequest) msg;

            // Store the received request:
            httpRequestFuture.set(httpRequest);

            // Write the given response:
            HttpResponse response = httpResponseRef.get();
            if (response != null)
            {
                ctx.writeAndFlush(response).sync();
            }
        }
    }

    public ListenableFuture<HttpRequest> getReceivedHttpRequest()
    {
        return httpRequestFuture;
    }

    public void setResponse(Object ciscoJaxbObject)
    {
        // Marshal object to XML:
        String objectAsXml = XmlMarshaller.marshalToXml(ciscoJaxbObject);

        // Build the content of the response body:
        byte[] bodyContent =
                objectAsXml.toString().getBytes(Charsets.ISO_8859_1);
        ByteBuf bodyContentByteBuf = Unpooled.wrappedBuffer(bodyContent);

        // Build the HTTP response:
        FullHttpResponse response =
                new DefaultFullHttpResponse(HTTP_1_1, OK, bodyContentByteBuf);
        response.headers().set(HttpHeaders.Names.CACHE_CONTROL,
                Arrays.asList(HttpHeaders.Values.MUST_REVALIDATE,
                              HttpHeaders.Values.NO_STORE));
        response.headers().set(HttpHeaders.Names.CONNECTION,
                HttpHeaders.Values.CLOSE);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE,
                               "text/html");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH,
                               bodyContent.length);

        httpResponseRef.set(response);
    }
}