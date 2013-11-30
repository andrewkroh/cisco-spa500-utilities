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

package com.andrewkroh.cisco.rtp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.NetUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.andrewkroh.cicso.rtp.Destination;
import com.andrewkroh.cicso.rtp.NettyRtpSession;
import com.andrewkroh.cicso.rtp.RtpPacket;

/**
 * If this test in running on a machine with IPv4 and IPv6 addresses then
 * it must be run with {@code -Djava.net.preferIPv4Stack=true}. This is needed
 * because the test uses an IPv4 multicast address which cannot be joined
 * from and IPv6 socket.
 *
 * <p/>
 * This test does not use IPv6.
 *
 * @author akroh
 */
public class NettyRtpSessionTest
{
    private static final byte PACKET_PAYLOAD_DATA = 123;

    private static final int PAYLOAD_TYPE = 84;

    private static final int TIMESTAMP = 9876543;

    private static final String PREFER_IPV4_KEY = "java.net.preferIPv4Stack";

    private static final String IPV4_MULTICAST_ADDRESS = "225.168.168.168";

    /**
     * {@link Bootstrap} for the test client.
     */
    private Bootstrap clientBootstrap;

    /**
     * Handler for the test client which will receive the messages
     * from the "server".
     */
    private TestHandler clientHandler;

    /**
     * {@link DatagramChannel} for the client. It is bound to
     * the IPv4 localhost on a random free port.
     */
    private DatagramChannel clientChannel;

    private final NetworkInterface multicastInterface = NetUtil.LOOPBACK_IF;

    /**
     * Session under test.
     */
    private NettyRtpSession session;


    @BeforeClass
    public static void beforeClass()
    {
        String preferIPv4Value = System.getProperty(PREFER_IPV4_KEY);

        if (preferIPv4Value == null || "false".equalsIgnoreCase(preferIPv4Value))
        {
            System.out.println("You may need to run this test with -D" +
                    PREFER_IPV4_KEY + "=true.");
        }
    }

    /**
     * Creates the test client and binds it to a random IPv4 address.
     */
    @Before
    public void beforeTest() throws InterruptedException, UnknownHostException
    {
        clientHandler = new TestHandler();
        clientBootstrap = new Bootstrap();
        clientBootstrap.group(new NioEventLoopGroup())
                       .channel(NioDatagramChannel.class)
                       .option(ChannelOption.SO_REUSEADDR, true)
                       .option(ChannelOption.IP_MULTICAST_IF, multicastInterface)
                       .localAddress(TestUtils.getFreePort())
                       .handler(clientHandler);

        clientChannel = (DatagramChannel) clientBootstrap.bind().sync().channel();
    }

    /**
     * Cleans up any of socket channels and the threads that service them.
     */
    @After
    public void afterTest()
    {
        if (session != null)
        {
            session.shutdown();
        }

        if (clientChannel != null)
        {
            clientChannel.close().awaitUninterruptibly();
        }

        if (clientBootstrap != null)
        {
            clientBootstrap.group().shutdownGracefully().awaitUninterruptibly();
        }
    }

    @Test(expected = NullPointerException.class)
    public void constructor_withNullBindAddress_throwsException()
    {
        session = new NettyRtpSession(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_multicastWithoutWildcard_throwsException()
    {
        session = new NettyRtpSession(new InetSocketAddress("127.0.0.1", TestUtils.getFreePort()),
                                      multicastInterface,
                                      multicastIPv4Addr());
    }

    @Test
    public void sendData_toMulticastDestination_packetIsReceived() throws Exception
    {
        InetAddress multicastGroup = multicastIPv4Addr();
        clientChannel.joinGroup(multicastGroup, multicastInterface, null);

        session = new NettyRtpSession(
                new InetSocketAddress(TestUtils.getFreePort()),
                multicastInterface,
                multicastGroup);
        session.addDestination(new Destination(multicastGroup.getHostAddress(),
                                               clientChannel.localAddress().getPort()));
        session.sendData(new byte[] {PACKET_PAYLOAD_DATA}, PAYLOAD_TYPE, TIMESTAMP);
        assertThatPayloadDataMatches(clientHandler.getOnlyReceivedPacket());
    }

    @Test
    public void sendData_toUnicastDestination_packetIsReceived() throws Exception
    {
        session = new NettyRtpSession(new InetSocketAddress(TestUtils.getFreePort()));
        session.addDestination(new Destination(NetUtil.LOCALHOST4.getHostAddress(),
                                               clientChannel.localAddress().getPort()));
        session.sendData(new byte[] {PACKET_PAYLOAD_DATA}, PAYLOAD_TYPE, TIMESTAMP);
        assertThatPayloadDataMatches(clientHandler.getOnlyReceivedPacket());
    }

    /**
     * Returns and IPv4 multicast {@link InetAddress}.
     */
    private static InetAddress multicastIPv4Addr()
    {
        try
        {
            return InetAddress.getByName(IPV4_MULTICAST_ADDRESS);
        }
        catch (UnknownHostException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Channel handler used by the test client in this test. It expects to
     * receive and single packet.
     */
    private static final class TestHandler extends SimpleChannelInboundHandler<DatagramPacket>
    {
        /**
         * Count down latch used to wait for <strong>one</strong> packet.
         */
        private final CountDownLatch latch = new CountDownLatch(1);

        /**
         * Contains the number of packets that were received.
         */
        private final AtomicInteger numberReceived = new AtomicInteger();

        /**
         * First RTP packet that was received by this handler.
         */
        private RtpPacket receivedPacket;

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception
        {
            if (numberReceived.incrementAndGet() == 1)
            {
                byte[] copy = new byte[msg.content().readableBytes()];
                msg.content().readBytes(copy);
                receivedPacket = new RtpPacket(copy);
            }

            latch.countDown();
        }

        /**
         * Gets first and only packet that was received by this handler. If no
         * packet was received or more than one packet was received then this
         * will throw an {@link AssertionError}. The method will block for 10
         * seconds to wait for the packet to be received.
         *
         * @return the first and only {@code RtpPacket} received
         *
         * @throws InterruptedException
         *             if interrupted while awaiting the packet
         */
        public RtpPacket getOnlyReceivedPacket()
                throws InterruptedException
        {
            if (latch.await(10, TimeUnit.SECONDS))
            {
                if (numberReceived.get() > 1)
                {
                    fail("Received more than one message.");
                }

                return receivedPacket;
            }
            else
            {
                fail("No message received.");
            }

            return null;
        }
    }

    /**
     * Assert that the given RtpPacket has the same payload data as the one that
     * was sent.
     *
     * @param packet
     *            {@code RtpPacket} to verify
     */
    private static void assertThatPayloadDataMatches(RtpPacket packet)
    {
        assertNotNull(packet);
        assertEquals(PACKET_PAYLOAD_DATA, packet.getRtpPayloadData()[0]);
        assertEquals(TIMESTAMP, packet.getTimestamp());
        assertEquals(PAYLOAD_TYPE, packet.getPayloadType());
    }
}
