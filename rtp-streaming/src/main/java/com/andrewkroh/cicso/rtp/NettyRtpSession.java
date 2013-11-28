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

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * {@link RtpSession} implementation that utilizes Netty for the underlying
 * network communications.
 *
 * @author akroh
 */
public class NettyRtpSession implements RtpSession, RtpPacketListener
{
    private static final Logger LOGGER =
            LoggerFactory.getLogger(NettyRtpSession.class);

    private static final int MULTICAST_TTL = 5;

    private static final int NUM_THREADS = 10;

    private static final String WILDCARD_ADDRESS = "0.0.0.0";

    private final Bootstrap bootstrap;

    private final DatagramChannel channel;

    private final CopyOnWriteArraySet<Destination> destinations =
            new CopyOnWriteArraySet<Destination>();

    public NettyRtpSession(final InetSocketAddress bindAddress)
    {
        this(bindAddress, null);
    }

    public NettyRtpSession(final InetSocketAddress bindAddress,
                           final InetAddress multicastGroup)
    {
        Preconditions.checkNotNull(bindAddress, "Must specify a bind address.");

        if (multicastGroup != null)
        {
            Preconditions.checkArgument(
                !WILDCARD_ADDRESS.equals(bindAddress.getAddress().getHostAddress()),
                "Cannot bind to wildcard address when using multicast.");
        }

        EventLoopGroup workerGroup = new NioEventLoopGroup(NUM_THREADS);

        bootstrap = new Bootstrap();
        bootstrap
            .group(workerGroup)
            .channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_REUSEADDR, true)
            .option(ChannelOption.IP_MULTICAST_TTL, MULTICAST_TTL)
            .localAddress(bindAddress)
            .handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception
                {
                    ch.pipeline().addLast(new RtpPacketHandler(NettyRtpSession.this));
                }
            });

        channel = (DatagramChannel) bootstrap.bind().syncUninterruptibly().channel();

        LOGGER.info("Session bound to: {}", channel.localAddress());

        if (multicastGroup != null)
        {
            channel.joinGroup(multicastGroup).syncUninterruptibly();

            LOGGER.info("Session bound to multicast group: {}", multicastGroup.getHostAddress());
        }
        else
        {
            LOGGER.info("Session will not be a multicast listener because " +
            		"no multicast group was specified.");
        }
    }

    public void shutdown()
    {
        try
        {
            channel.close().sync();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
        finally
        {
            bootstrap.group().shutdownGracefully();
        }
    }

    @Override
    public boolean addDestination(Destination destination)
    {
        return destinations.add(destination);
    }

    @Override
    public boolean removeDestination(Destination destination)
    {
        return destinations.remove(destination);
    }

    @Override
    public Collection<Destination> getDestinations()
    {
        return new HashSet<Destination>(destinations);
    }

    @Override
    public void sendData(byte[] data, int payloadType, long timestamp)
    {
        Preconditions.checkNotNull(data);

        RtpPacket packet = new RtpPacket();
        packet.setRtpPayloadData(data);
        packet.setTimestamp((int)timestamp);
        packet.setPayloadType(payloadType);

        sendData(packet);
    }

    @Override
    public void sendData(RtpPacket rtpPacket)
    {
        Preconditions.checkNotNull(rtpPacket);

        ByteBuf buffer = Unpooled.copiedBuffer(rtpPacket.getBytes());

        for (Destination destination : destinations)
        {
                channel.writeAndFlush(new DatagramPacket(
                        buffer, destination.getSocketAddress()));
        }
    }

    @Override
    public void packetReceived(InetSocketAddress source,
                               InetSocketAddress receiver,
                               RtpPacket packet)
    {
        LOGGER.debug("RtpPacket received from {}, sent to {}: {}",
                source, receiver, packet);
    }
}
