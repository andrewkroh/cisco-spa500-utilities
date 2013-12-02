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

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractScheduledService;

/**
 * Streams an audio file using an {@link RtpSession} as the distribution
 * mechanism.
 *
 * @author akroh
 */
public class AudioFileStreamer extends AbstractScheduledService
{
    /**
     * Supported encoding types of this class.
     */
    public enum EncodingType
    {
        /**
         * RFC3551 specifies payload type 8 for PCMA (G.711), 8000 Hz, 1 channel.
         */
        ALAW(8),

        /**
         * Dynamic payload type for L16, 8000 Hz, 1 channel.
         * This is a custom type defined here.
         */
        PCM16(96),

        /**
         * RFC3551 specifies payload type 0 for PCMU, 8000 Hz, 1 channel.
         */
        ULAW(0);

        private EncodingType(int payloadType)
        {
            this.payloadType = payloadType;
        }

        public int getPayloadType()
        {
            return payloadType;
        }

        private final int payloadType;
    }

    /**
     * SLF4J Logger for this class.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(AudioFileStreamer.class);

    /**
     * AudioFormat of the output stream.
     */
    private final AudioFormat outputFormat;

    /**
     * EncodingType used for the output.
     */
    private final EncodingType outputEncodingType;

    /**
     * Playback length of the audio data in a single
     * packet given in milliseconds.
     */
    private final long outputPacketLengthMs;

    /**
     * Number of samples in one packet.
     */
    private final int numSamplesPerPacket;

    /**
     * Payload size of a single packet given in bytes.
     */
    private final int payloadSizeBytes;

    /**
     * {@code RtpSession} used for streaming.
     */
    private final RtpSession rtpSession;

    /**
     * {@code ByteBuffer} containing the source's complete audio data in
     * the output encoding.
     */
    private final ByteBuffer sourceDataBuffer;

    /**
     * URL of the source file.
     */
    private final URL sourceUrl;

    private final Random randomNumberGen = new Random();

    /**
     * SSRC that is used in RtpPackets.
     */
    private final int ssrc = randomNumberGen.nextInt();

    /**
     * Timestamp that is used in RtpPackets. Per RFC3550 the timestamp
     * is initialized to a random value.
     */
    private int timestamp = randomNumberGen.nextInt();

    /**
     * Sequence number that is used in RtpPackets.
     */
    private int sequenceNumber = 0;

    /**
     * Constructs a new AudioFileStreamer whose source data will be read from
     * the specified URL. The encoding on the output stream will be the
     * specified {@code outputEncoding} value. Each RTP packet will contain the
     * corresponding number of samples to represent the amount of time given in
     * {@code outputPacketLengthMs}.
     *
     * @param sourceUrl
     *            URL of the source file
     * @param outputEncoding
     *            encoding type to use for the output data
     * @param outputPacketLengthMs
     *            amount of data to put into each packet
     * @param rtpSession
     *            {@code RtpSession} to use for streaming the data
     * @throws UnsupportedAudioFileException
     *             if the source file is in an unsupported format or if the
     *             source file cannot be converted to the specifed encoding type
     * @throws IOException
     *             if there is problem reading the source file
     */
    public AudioFileStreamer(URL sourceUrl,
                             EncodingType outputEncoding,
                             long outputPacketLengthMs,
                             RtpSession rtpSession)
            throws UnsupportedAudioFileException, IOException
    {
        this.sourceUrl = Preconditions.checkNotNull(sourceUrl,
                "Audio file source URL cannot be null.");
        this.outputEncodingType = Preconditions.checkNotNull(outputEncoding,
                "Output encoding type cannot be null.");
        this.rtpSession = Preconditions.checkNotNull(rtpSession,
                "RtpSession cannot be null.");
        this.outputPacketLengthMs = outputPacketLengthMs;

        // Read input source:
        AudioInputStream sourceStream =
                AudioSystem.getAudioInputStream(sourceUrl);
        AudioFormat conversionFormat = getConversionFormat(
                sourceStream.getFormat(), outputEncoding);
        LOGGER.debug("Input format: {}",
                audioFormatToString(sourceStream.getFormat()));
        LOGGER.debug("Conversion format: {}",
                audioFormatToString(conversionFormat));

        // Convert to output format:
        AudioInputStream outputStream =
                AudioSystem.getAudioInputStream(conversionFormat, sourceStream);
        outputFormat = outputStream.getFormat();
        LOGGER.debug("Output format: {}", audioFormatToString(outputFormat));

        // Buffer the output data:
        sourceDataBuffer = ByteBuffer.allocate(outputStream.available());
        outputStream.read(sourceDataBuffer.array(), 0, sourceDataBuffer.capacity());

        // Calculate packet size:
        numSamplesPerPacket = getNumberOfSamplesPerTimePeriod(outputFormat,
                outputPacketLengthMs, TimeUnit.MILLISECONDS);
        int sampleSizeBytes = outputStream.getFormat().getSampleSizeInBits() / 8;
        payloadSizeBytes = numSamplesPerPacket * sampleSizeBytes;
    }

    /**
     * Returns the AudioFormat of the output stream.
     *
     * @return AudioFormat of the output stream
     */
    public AudioFormat getOutputFormat()
    {
        return outputFormat;
    }

    /**
     * Returns the playback length in milliseconds of the data contained in each
     * output packet.
     *
     * @return playback length in milliseconds of the data contained in each
     *         output packet
     */
    public long getOutputPacketLengthMs()
    {
        return outputPacketLengthMs;
    }

    /**
     * Returns the RtpSession used for streaming data.
     *
     * @return RtpSession used for streaming data
     */
    public RtpSession getRtpSession()
    {
        return rtpSession;
    }

    /**
     * Returns the URL of the source audio file.
     *
     * @return URL of the source audio file
     */
    public URL getSourceUrl()
    {
        return sourceUrl;
    }

    @Override
    protected void runOneIteration() throws Exception
    {
        try
        {
            sendAudioData();
        }
        catch (RuntimeException e)
        {
            LOGGER.error("An exception occurred while sending audio data.", e);
        }
    }

    @Override
    protected Scheduler scheduler()
    {
        return Scheduler.newFixedRateSchedule(0,
                outputPacketLengthMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Sends a single packet of audio data. It reads from the
     * {@link #sourceDataBuffer} and will rewind that buffer when it reaches the
     * end so that it continuously streams the source file in a loop.
     */
    private void sendAudioData()
    {
        ByteBuffer packetDataBuffer = ByteBuffer.allocate(payloadSizeBytes);

        while (packetDataBuffer.hasRemaining())
        {
            if (!sourceDataBuffer.hasRemaining())
            {
                sourceDataBuffer.rewind();
            }

            packetDataBuffer.put(sourceDataBuffer.get());
        }

        timestamp += numSamplesPerPacket;

        RtpPacket packet = new RtpPacket();
        packet.setPayloadType(outputEncodingType.getPayloadType());
        packet.setSSRC(ssrc);
        packet.setSequenceNumber(++sequenceNumber);
        packet.setTimestamp(timestamp);
        packet.setRtpPayloadData(packetDataBuffer.array());

        rtpSession.sendData(packet);
    }

    /**
     * Utility method to convert an {@link AudioFormat} object to a String.
     * {@code AudioFormat} does implement a toString method, but it's output
     * varies depending upon the contents. I find it more useful to always print
     * the value of all fields.
     *
     * @param format
     *            {@code AudioFormat} to convert to a String
     * @return {@code AudioFormat} object as a String
     */
    private static String audioFormatToString(AudioFormat format)
    {
        return new ToStringBuilder(format)
                    .append("encoding", format.getEncoding())
                    .append("sampleRate", format.getSampleRate())
                    .append("sampleSizeInBits", format.getSampleSizeInBits())
                    .append("channels", format.getChannels())
                    .append("frameSize", format.getFrameSize())
                    .append("frameRate", format.getFrameRate())
                    .append("isBigEndian", format.isBigEndian())
                    .toString();
    }

    /**
     * Builds an AudioFormat object used for converting to the specified output
     * encoding type. The format will be used with
     * {@link AudioSystem#getAudioInputStream(AudioFormat, AudioInputStream)}.
     *
     * @param source
     *            {@code AudioFormat} of the source
     * @param outputEncoding
     *            encoding type of the output
     * @return audio format used for converting to the specified
     *         {@code outputEncoding} type
     */
    private static AudioFormat getConversionFormat(
            AudioFormat source, EncodingType outputEncoding)
    {
        Preconditions.checkNotNull(source,
                "Source AudioFormat cannot be null.");
        Preconditions.checkNotNull(outputEncoding,
                "Output EncodingType cannot be null.");

        switch (outputEncoding)
        {
            case ALAW:
                return toAlawFormat(source);
            case PCM16:
                return toPcm16Format(source, false);
            case ULAW:
                return toUlawFormat(source);
            default:
                throw new IllegalArgumentException(
                        "Unhandled EncodingType: " + outputEncoding.name());
        }
    }

    /**
     * Returns the number of samples that represent the specified time period.
     *
     * @param outputFormat
     *            format of the data, needed to obtain the sample rate
     * @param timePeriod
     *            period of time
     * @param timeUnit
     *            TimeUnit of the {@code timePeriod}
     * @return number of samples that represent the specified time period
     */
    @VisibleForTesting
    public static int getNumberOfSamplesPerTimePeriod(
            AudioFormat outputFormat, long timePeriod, TimeUnit timeUnit)
    {
        double timePeriodSec = timeUnit.toNanos(timePeriod) / 1E9;
        double numberOfSamples = timePeriodSec * outputFormat.getSampleRate();
        return (int) Math.ceil(numberOfSamples);
    }

    private static AudioFormat toAlawFormat(AudioFormat source)
    {
        Preconditions.checkNotNull(source,
                "Source AudioFormat cannot be null.");

        return new AudioFormat(AudioFormat.Encoding.ALAW,
                               source.getSampleRate(),
                               8, // sample size in bits
                               source.getChannels(),
                               1, // frame size in bytes
                               source.getFrameRate(),
                               source.isBigEndian());
    }

    private static AudioFormat toPcm16Format(AudioFormat source, boolean bigEndianOutput)
    {
        Preconditions.checkNotNull(source,
                "Source AudioFormat cannot be null.");

        return new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED,
                               source.getSampleRate(),
                               16, // sample size in bits
                               source.getChannels(),
                               2, // frame size in bytes
                               source.getFrameRate(),
                               bigEndianOutput);
    }

    private static AudioFormat toUlawFormat(AudioFormat source)
    {
        Preconditions.checkNotNull(source,
                "Source AudioFormat cannot be null.");

        return new AudioFormat(AudioFormat.Encoding.ULAW,
                               source.getSampleRate(),
                               8, // sample size in bits
                               source.getChannels(),
                               1, // frame size in bytes
                               source.getFrameRate(),
                               source.isBigEndian());
    }
}
