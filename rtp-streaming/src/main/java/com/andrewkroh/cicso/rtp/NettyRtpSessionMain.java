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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

/**
 * Stand-alone RTP listener that uses {@link NettyRtpSession}.
 *
 * @author akroh
 */
public class NettyRtpSessionMain
{
    /**
     * Struct containing the command line arguments.
     */
    private static class Arguments
    {
        @Parameter(names={"--host", "-h"}, required = false,
                   description = "Local bind address, defaults to wildcard.")
        private String host;

        @Parameter(names={"--port", "-p"}, required = true,
                   description = "Local bind port.")
        private Integer port;

        @Parameter(names={"--interface", "-i"}, required=false,
                   description = "Network interface to use for multicast. " +
                   		         "Must use with --group.")
        private String multicastInterface;

        @Parameter(names={"--group", "-g"}, required = false,
                   description = "Multicast group to join. Must use " +
                   		         "with --interface.")
        private String multicastGroup;
    }

    /**
     * Prints the command line usage information.
     *
     * @param jcommander
     *            the JCommander object which knows the expected arguments
     */
    private static void printUsage(JCommander jcommander)
    {
        jcommander.setProgramName("rtp-listener");
        jcommander.usage();
    }

    public static void main(String[] args)
            throws UnknownHostException, SocketException
    {
        System.setProperty("java.net.preferIPv4Stack" , "true");

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

        InetSocketAddress bindAddress;

        if (arguments.host == null)
        {
            bindAddress = new InetSocketAddress(arguments.port);
        }
        else
        {
            bindAddress = new InetSocketAddress(arguments.host, arguments.port);
        }

        if (arguments.multicastInterface == null &&
                arguments.multicastGroup == null)
        {
            new NettyRtpSession(bindAddress);
        }
        else if (arguments.multicastInterface != null &&
                arguments.multicastGroup != null)
        {
            NetworkInterface mcastInterface =
                    NetworkInterface.getByName(arguments.multicastInterface);

            InetAddress multicastGroup =
                    InetAddress.getByName(arguments.multicastGroup);

            new NettyRtpSession(bindAddress, mcastInterface, multicastGroup);
        }
        else
        {
            printUsage(jcommander);
            System.exit(1);
        }
    }
}
