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

package com.andrewkroh.cicso.rtp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import com.google.common.base.Preconditions;

/**
 * Netty inbound channel handler that is responsible for receiving
 * the inbound UDP data, converting it to RTP, and this distributing it.
 *
 * @author akroh
 */
public class RtpPacketHandler extends SimpleChannelInboundHandler<DatagramPacket>
{
    private final RtpPacketListener listener;

    public RtpPacketHandler(RtpPacketListener listener)
    {
        this.listener = Preconditions.checkNotNull(listener,
                            "RtpPacketListener cannot be null.");
    }

    // NOTE: In Netty 5.0 this method will be renamed to messageRead().
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg)
            throws Exception
    {
        if (msg.content().readableBytes() > RtpPacket.FIXED_HEADER_SIZE)
        {
            byte[] bufferCopy = new byte[msg.content().readableBytes()];
            msg.content().readBytes(bufferCopy);
            RtpPacket packet = new RtpPacket(bufferCopy);

            listener.packetReceived(msg.sender(), msg.recipient(), packet);
        }
    }
}
