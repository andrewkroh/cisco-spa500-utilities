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
import java.net.NetworkInterface;
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

    private final Bootstrap bootstrap;

    private final DatagramChannel channel;

    private final CopyOnWriteArraySet<Destination> destinations =
            new CopyOnWriteArraySet<Destination>();

    public NettyRtpSession(final InetSocketAddress bindAddress)
    {
        this(bindAddress, null, null);
    }

    public NettyRtpSession(final InetSocketAddress bindAddress,
                           final NetworkInterface multicastInterface,
                           final InetAddress multicastGroup)
    {
        Preconditions.checkNotNull(bindAddress, "Must specify a bind address.");

        if (multicastInterface != null)
        {
            Preconditions.checkNotNull(multicastGroup,
                    "When specifying the multicast interface you must " +
                    "also specify the multicast group.");
        }

        if (multicastGroup != null)
        {
            Preconditions.checkNotNull(multicastInterface,
                    "When specifying the multicast group you must also " +
                    "specify the multicast interface.");

            // Javadoc for Java 7 MulticastChannel states: The channel's
            // socket should be bound to the wildcard address. If the socket
            // is bound to a specific address, rather than the wildcard address
            // then it is implementation specific if multicast datagrams
            // are received by the socket.
            Preconditions.checkArgument(
                bindAddress.getAddress().isAnyLocalAddress(),
                "Must bind to wildcard address when using multicast.");
        }

        EventLoopGroup workerGroup = new NioEventLoopGroup(NUM_THREADS);

        bootstrap = new Bootstrap();
        bootstrap
            .group(workerGroup)
            .channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_REUSEADDR, true)
            .localAddress(bindAddress)
            .handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception
                {
                    ch.pipeline().addLast(new RtpPacketHandler(NettyRtpSession.this));
                }
            });

        if (multicastGroup != null)
        {
            bootstrap.option(ChannelOption.IP_MULTICAST_TTL, MULTICAST_TTL);

            // All multicast traffic generated from this channel will be
            // output from this interface. If not specified it will use the OS
            // default which may be unpredicatable:
            bootstrap.option(ChannelOption.IP_MULTICAST_IF, multicastInterface);
        }

        channel = (DatagramChannel) bootstrap.bind().syncUninterruptibly().channel();

        LOGGER.info("Session bound to: {}", channel.localAddress());

        if (multicastGroup != null)
        {
            channel.joinGroup(multicastGroup, multicastInterface, null).syncUninterruptibly();

            LOGGER.info("Session bound to multicast group {} on interface {}.",
                    multicastGroup.getHostAddress(),
                    multicastInterface.getDisplayName());
        }
        else
        {
            LOGGER.info("Session will not be a multicast listener because " +
            		"no multicast group was specified.");
        }
    }

    /**
     * Shuts down the session, closes the socket, and cleans up any threads.
     * Calling any methods on the session after it has been shutdown can result
     * in undefined behavior.
     *
     * @throws IllegalStateException
     *             if {@code shutdown} has already been called
     */
    public void shutdown()
    {
        checkNotShutdown();

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

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException
     *             if {@code shutdown} has already been called
     */
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

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException
     *             if {@code shutdown} has already been called
     */
    @Override
    public void sendData(RtpPacket rtpPacket)
    {
        Preconditions.checkNotNull(rtpPacket);
        checkNotShutdown();

        for (Destination destination : destinations)
        {
            ByteBuf buffer = Unpooled.copiedBuffer(rtpPacket.getBytes());
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

    /**
     * Checks if {@link #shutdown()} has been called and throws an
     * {@link IllegalStateException} if it has.
     *
     * @throws IllegalStateException
     *             if {@code shutdown} has already been called
     */
    private void checkNotShutdown()
    {
        if (bootstrap.group().isShuttingDown())
        {
            throw new IllegalStateException(
                    "NettyRtpSession has already been shutdown.");
        }
    }
}
