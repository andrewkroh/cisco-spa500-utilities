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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.andrewkroh.cicso.rtp.AudioFileStreamer.EncodingType;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

/**
 * Stand-alone application for streaming audio files via RTP.
 *
 * @author akroh
 */
public class AudioFileStreamerMain
{
    /**
     * Struct containing the command line arguments.
     */
    private static class Arguments
    {
        @Parameter(names={"--host", "-h"},
                   description = "Local bind address, defaults to wildcard.")
        private String host;

        @Parameter(names={"--port", "-p"},
                   description = "Local bind port.")
        private int port = 22222;

        @Parameter(names={"--interface", "-i"},
                   description = "Network interface to use for multicast. " +
                   		         "Must use with --group.")
        private String multicastInterface;

        @Parameter(names={"--group", "-g"},
                   description = "Multicast group to join. Must use " +
                   		         "with --interface.")
        private String multicastGroup;

        @Parameter(names={"--encoding", "-e"},
                   description = "Encoding type of the output data stream. " +
                   		         "Options are alaw, ulaw, pcm16.")
        private String outputEncoding = "ulaw";

        @Parameter(names={"--file", "-f"}, required = true,
                description = "Source wave file that will be streamed.")
        private String sourceFile;

        @Parameter(names={"--destinations", "-d"}, required = true,
                   variableArity = true,
                   description = "Stream destinations separated by spaces. " +
                   		         "Format destinations as host:port.")
        private List<String> destinations;
    }

    private static EncodingType parseEncoding(String encoding)
    {
        for (EncodingType enumValue : EncodingType.values())
        {
            if (enumValue.name().equalsIgnoreCase(encoding))
            {
                return enumValue;
            }
        }

        throw new IllegalArgumentException(
                "Unknown encoding type: " + encoding);
    }

    private static List<Destination> parseDestinations(List<String> stringDestinations)
    {
        List<Destination> destinations = new ArrayList<Destination>();

        for (String stringDestination : stringDestinations)
        {
            String[] hostPort = stringDestination.trim().split(":");

            if (hostPort == null ||
                    hostPort.length != 2 ||
                    hostPort[0].trim().isEmpty() ||
                    !hostPort[1].matches("[0-9]+"))
            {
                throw new IllegalArgumentException(
                        "Destination <" + stringDestination + "> is invalid. " +
                        "Must be of the form host:port.");
            }


            destinations.add(new Destination(hostPort[0].trim(),
                                             Integer.valueOf(hostPort[1].trim())));
        }

        return destinations;
    }

    /**
     * Prints the command line usage information.
     *
     * @param jcommander
     *            the JCommander object which knows the expected arguments
     */
    private static void printUsage(JCommander jcommander)
    {
        jcommander.setProgramName("rtp-streaming");
        jcommander.usage();
    }

    public static void main(String[] args)
            throws UnsupportedAudioFileException, IOException
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

        // --- Bind Address ---
        InetSocketAddress bindAddress;
        if (arguments.host == null)
        {
            bindAddress = new InetSocketAddress(arguments.port);
        }
        else
        {
            bindAddress = new InetSocketAddress(arguments.host, arguments.port);
        }

        // --- RtpSession ---
        RtpSession rtpSession = null;
        if (arguments.multicastInterface == null &&
                arguments.multicastGroup == null)
        {
            rtpSession = new NettyRtpSession(bindAddress);
        }
        else if (arguments.multicastInterface != null &&
                arguments.multicastGroup != null)
        {
            NetworkInterface mcastInterface =
                    NetworkInterface.getByName(arguments.multicastInterface);

            InetAddress multicastGroup =
                    InetAddress.getByName(arguments.multicastGroup);

            rtpSession = new NettyRtpSession(bindAddress, mcastInterface, multicastGroup);
        }
        else
        {
            printUsage(jcommander);
            System.exit(1);
        }

        // --- Source URL ---
        URL sourceUrl = null;
        if (arguments.sourceFile != null)
        {
            sourceUrl = new File(arguments.sourceFile).toURI().toURL();
        }
        else
        {
            printUsage(jcommander);
            System.exit(1);
        }

        // --- Destinations ---
        if (arguments.destinations != null && !arguments.destinations.isEmpty())
        {
            List<Destination> destinations = parseDestinations(arguments.destinations);

            for (Destination destination : destinations)
            {
                rtpSession.addDestination(destination);
            }
        }
        else
        {
            printUsage(jcommander);
            System.exit(1);
        }

        // --- EncodingType ---
        EncodingType encodingType = parseEncoding(arguments.outputEncoding);

        // --- Start Streaming ---
        AudioFileStreamer streamer = new AudioFileStreamer(
                sourceUrl, encodingType, 20, rtpSession);
        streamer.startAsync().awaitRunning();
    }
}