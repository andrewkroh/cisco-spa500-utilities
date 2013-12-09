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

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.xmlservices.XmlMarshaller;
import com.cisco.xmlservices.generated.CiscoIPPhoneResponse;
import com.google.common.util.concurrent.SettableFuture;

@Sharable
public class CiscoXmlResponseChannelHandler extends
        SimpleChannelInboundHandler<HttpObject>
{
    private static final Logger LOGGER =
            LoggerFactory.getLogger(CiscoXmlResponseChannelHandler.class);

    private final AttributeKey<CiscoIpPhone> phoneAttributeKey;

    private final AttributeKey<SettableFuture<CiscoXmlPushResponse>> responseAttributeKey;

    public CiscoXmlResponseChannelHandler(
            AttributeKey<CiscoIpPhone> phoneAttributeKey,
            AttributeKey<SettableFuture<CiscoXmlPushResponse>> responseAttributeKey)
    {
        this.phoneAttributeKey = phoneAttributeKey;
        this.responseAttributeKey = responseAttributeKey;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg)
            throws Exception
    {
        CiscoIpPhone phone = ctx.channel().attr(phoneAttributeKey).get();

        if (msg instanceof HttpResponse && LOGGER.isDebugEnabled())
        {
            HttpResponse response = (HttpResponse) msg;

            StringBuilder responseInfo = new StringBuilder();
            responseInfo.append("Source=");
            responseInfo.append(phone.getHostname());
            responseInfo.append(", ");
            responseInfo.append("Status=");
            responseInfo.append(response.getStatus());
            responseInfo.append(", ");
            responseInfo.append("Version=");
            responseInfo.append(response.getProtocolVersion());

            for (String name : response.headers().names())
            {
                for (String value : response.headers().getAll(name))
                {
                    responseInfo.append(", ");
                    responseInfo.append(name);
                    responseInfo.append('=');
                    responseInfo.append(value);
                }
            }

            LOGGER.debug(responseInfo.toString());
        }

        if (msg instanceof HttpContent)
        {
            HttpContent content = (HttpContent) msg;

            SettableFuture<CiscoXmlPushResponse> responseFuture =
                    ctx.channel().attr(responseAttributeKey).get();

            // The default charset for HTTP is ISO-8859-1. None
            // of the Cisco phones I've seen to date were actually
            // setting the charset so use the default. We could
            // improve here by checking the header for the value.
            String xmlContent = content.content().toString(
                    CharsetUtil.ISO_8859_1);

            CiscoIPPhoneResponse xmlResponse =
                XmlMarshaller.unmarshal(xmlContent, CiscoIPPhoneResponse.class);
            responseFuture.set(new DefaultCiscoXmlPushResponse(phone, xmlResponse));

            // Cleanup:
            ctx.close();
            ctx.channel().close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception
    {
        if (LOGGER.isWarnEnabled())
        {
            CiscoIpPhone phone = ctx.channel().attr(phoneAttributeKey).get();
            LOGGER.warn("Exception in handler for {}.", phone.getHostname());
        }

        SettableFuture<CiscoXmlPushResponse> responseFuture = ctx.channel()
                .attr(responseAttributeKey).get();
        responseFuture.setException(cause);

        // Cleanup:
        ctx.close();
        ctx.channel().close();
    }
}
