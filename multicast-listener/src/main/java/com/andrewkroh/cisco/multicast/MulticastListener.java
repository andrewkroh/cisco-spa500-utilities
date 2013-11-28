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

package com.andrewkroh.cisco.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.andrewkroh.cisco.common.HexUtility;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

/**
 * Utility for joining a multicast group and echoing out the data that
 * is received in hex.
 *
 * @author akroh
 */
public class MulticastListener
{
    /**
     * Struct containing the command line arguments.
     */
    private static class Arguments
    {
        @Parameter(names={"--host", "-h"}, required = true)
        private String multicastHost;

        @Parameter(names={"--port", "-p"}, required = true)
        private int port;
    }

    /**
     * SLF4J logger for this class.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(MulticastListener.class);

    /**
     * The maximum UDP message size.
     *
     * <pre>
     * 0xffff - (sizeof(IP Header) + sizeof(UDP Header)) = 65535-(20+8) = 65507
     * </pre>
     */
    private static final int MAX_UDP_SIZE = 65507;

    /**
     * Parses the command line arguments, joins the specified multicast group,
     * and prints out all the data it receives in hex. User must Ctrl+C to quit.
     *
     * @param args
     *            command line arguments as as String array
     */
    public static void main(String[] args)
    {
        final Arguments arguments = new Arguments();
        JCommander jcommander = new JCommander(arguments);

        try
        {
            jcommander.parse(args);
        }
        catch (ParameterException e)
        {
            printUsage(jcommander);
            System.exit(1);
        }

        try
        {
            InetAddress multicastGroupAddress =
                    InetAddress.getByName(arguments.multicastHost);
            MulticastSocket multicastSocket = new MulticastSocket(arguments.port);
            multicastSocket.joinGroup(multicastGroupAddress);

            LOGGER.info("Joined multicast group {}.", multicastGroupAddress.getHostAddress());

            byte[] buffer = new byte[MAX_UDP_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (true)
            {
                multicastSocket.receive(packet);

                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Received: {}", HexUtility.bytesToHex(
                            Arrays.copyOfRange(packet.getData(), 0,
                                               packet.getLength())));
                }
            }
        }
        catch (IOException e)
        {
            LOGGER.warn("IOException while receiving data.", e);
        }
    }

    /**
     * Prints the command line usage information.
     *
     * @param jcommander
     *            the JCommander object which knows the expected arguments
     */
    private static void printUsage(JCommander jcommander)
    {
        jcommander.setProgramName("multicast-listener");
        jcommander.usage();
    }
}
