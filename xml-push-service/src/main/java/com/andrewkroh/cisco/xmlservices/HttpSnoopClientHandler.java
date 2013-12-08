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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import java.util.Collection;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.cisco.xmlservices.XmlMarshaller;
import com.google.common.util.concurrent.SettableFuture;

@Sharable
public class HttpSnoopClientHandler<T> extends
        SimpleChannelInboundHandler<HttpObject>
{
    private final AttributeKey<SettableFuture<T>> responseAttributeKey;

    public HttpSnoopClientHandler(AttributeKey<SettableFuture<T>> responseAttributeKey)
    {
        this.responseAttributeKey = responseAttributeKey;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg)
            throws Exception
    {
        if (msg instanceof HttpResponse)
        {
            HttpResponse response = (HttpResponse) msg;

            System.out.println("STATUS: " + response.getStatus());
            System.out.println("VERSION: " + response.getProtocolVersion());
            System.out.println();

            if (!response.headers().isEmpty())
            {
                for (String name : response.headers().names())
                {
                    for (String value : response.headers().getAll(name))
                    {
                        System.out.println("HEADER: " + name + " = " + value);
                    }
                }
                System.out.println();
            }

            if (HttpHeaders.isTransferEncodingChunked(response))
            {
                System.out.println("CHUNKED CONTENT {");
            }
            else
            {
                System.out.println("CONTENT {");
            }
        }
        if (msg instanceof HttpContent)
        {
            HttpContent content = (HttpContent) msg;
            System.out.println("Content:");

            System.out.print(content.content().toString(CharsetUtil.UTF_8));
            System.out.flush();

            if (content instanceof LastHttpContent)
            {
                SettableFuture<T> responseFuture = ctx.channel()
                        .attr(responseAttributeKey).get();
                try
                {
                    // TODO: set proper encoding based on header
                    String xmlContent = content.content().toString(
                            CharsetUtil.UTF_8);

                    @SuppressWarnings("unchecked")
                    T xmlResponse = (T) XmlMarshaller.unmarshal(xmlContent);
                    responseFuture.set(xmlResponse);
                    System.out.println(ToStringBuilder.reflectionToString(
                            xmlResponse, new RecursiveToStringStyle(5)));
                }
                catch (RuntimeException e)
                {
                    responseFuture.setException(e);
                }

                System.out.println("} END OF CONTENT");
            }
            else
            {

            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception
    {
        cause.printStackTrace();
        ctx.close();
    }

    private static class RecursiveToStringStyle extends ToStringStyle
    {
        private static final int INFINITE_DEPTH = -1;

        /**
         * Setting {@link #maxDepth} to 0 will have the same effect as using
         * original {@link #ToStringStyle}: it will print all 1st level values
         * without traversing into them. Setting to 1 will traverse up to 2nd
         * level and so on.
         */
        private int maxDepth;

        private int depth;

        public RecursiveToStringStyle()
        {
            this(INFINITE_DEPTH);
        }

        public RecursiveToStringStyle(int maxDepth)
        {
            setUseShortClassName(true);
            setUseIdentityHashCode(false);

            this.maxDepth = maxDepth;
        }

        @Override
        protected void appendDetail(StringBuffer buffer, String fieldName,
                Object value)
        {
            if (value.getClass().getName().startsWith("java.lang.")
                    || (maxDepth != INFINITE_DEPTH && depth >= maxDepth))
            {
                buffer.append(value);
            }
            else
            {
                depth++;
                buffer.append(ReflectionToStringBuilder.toString(value, this));
                depth--;
            }
        }

        // another helpful method
        @Override
        protected void appendDetail(StringBuffer buffer, String fieldName,
                Collection<?> coll)
        {
            depth++;
            buffer.append(ReflectionToStringBuilder.toString(coll.toArray(),
                    this, true, true));
            depth--;
        }
    }
}
