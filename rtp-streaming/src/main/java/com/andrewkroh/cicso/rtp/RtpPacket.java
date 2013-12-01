/*
 * Copyright 2010 Andrew Kroh
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

import java.util.Random;

/**
 * A straightforward representation of an RTP packet. It can be used
 * to interpret an incoming RTP packet read off the wire or can be used
 * to create an RTP from scratch for sending.
 *
 * <p> RTP Header Format:
 * <pre>
 *     0                   1                   2                   3
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |V=2|P|X|  CC   |M|     PT      |       sequence number         |
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |                           timestamp                           |
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |           synchronization source (SSRC) identifier            |
 *  +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+
 *  |            contributing source (CSRC) identifiers             |
 *  |                             ....                              |
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 *
 * <p>
 * If X bit (extension header) is set this will follow the
 * standard RTP header:
 * </p>
 *
 * <pre>
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |      defined by profile       |           length              |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                        header extension                       |
 * |                             ....                              |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  </pre>
 *
 * @author     Original Author: akroh
 */
public class RtpPacket
{
    /**
     * Fixed size of the header that accompanies every RTP
     * packet. This does not include the size of any
     * additional CSRC identifiers.
     */
    protected static final int FIXED_HEADER_SIZE = 12;

    /**
     * Default and standard RTP version number.
     */
    protected static final int DEFAULT_VERSION = 2;

    /**
     * RTP version number.
     */
    protected int version = DEFAULT_VERSION;

    /**
     * If set, this packet contains one or more additional padding bytes at the
     * end which are not part of the payload. The last byte of the padding
     * contains a count of how many padding bytes should be ignored. Padding may
     * be needed by some encryption algorithms with fixed block sizes or for
     * carrying several RTP packets in a lower-layer protocol data unit.
     */
    protected boolean padding = false;

    /**
     * If set, the fixed header is followed by exactly one header extension.
     */
    protected boolean extension = false;

    /**
     * Distinguishing identifier from the first 16 bits of the header
     * extension.
     */
    protected int extensionHeaderId = 0;

    /**
     * Data from the extension header.
     */
    protected byte[] extensionHeaderData = new byte[0];

    /**
     * The number of CSRC identifiers that follow the fixed header.
     */
    protected int CSRCCount = 0;

    /**
     * The interpretation of the marker is defined by a profile. It is intended
     * to allow significant events such as frame boundaries to be marked in the
     * packet stream. A profile may define additional marker bits or specify
     * that there is no marker bit by changing the number of bits in the payload
     * type field.
     */
    protected boolean marker = false;

    /**
     * Identifies the format of the RTP payload and determines its
     * interpretation by the application. A profile specifies a default static
     * mapping of payload type codes to payload formats. Additional payload type
     * codes may be defined dynamically through non-RTP means. An RTP sender
     * emits a single RTP payload type at any given time; this field is not
     * intended for multiplexing separate media streams.
     */
    protected int payloadType = 0;

    /**
     * The sequence number increments by one for each RTP data packet sent, and
     * may be used by the receiver to detect packet loss and to restore packet
     * sequence. The initial value of the sequence number is random
     * (unpredictable) to make known-plaintext attacks on encryption more
     * difficult, even if the source itself does not encrypt, because the
     * packets may flow through a translator that does.
     */
    protected int sequenceNumber = 0;


    /**
     * The largest sequence number that can be sent in an RTP packet.
     * Value is 2^16 - 1.
     */
    public static final int MAX_SEQUENCE_NUM = 65535;

    /**
     * The timestamp reflects the sampling instant of the first octet in the RTP
     * data packet. The sampling instant must be derived from a clock that
     * increments monotonically and linearly in time to allow synchronization
     * and jitter calculations. The resolution of the clock must be sufficient
     * for the desired synchronization accuracy and for measuring packet arrival
     * jitter (one tick per video frame is typically not sufficient). The clock
     * frequency is dependent on the format of data carried as payload and is
     * specified statically in the profile or payload format specification that
     * defines the format, or may be specified dynamically for payload formats
     * defined through non-RTP means. If RTP packets are generated periodically,
     * the nominal sampling instant as determined from the sampling clock is to
     * be used, not a reading of the system clock. As an example, for fixed-rate
     * audio the timestamp clock would likely increment by one for each sampling
     * period. If an audio application reads blocks covering 160 sampling
     * periods from the input device, the timestamp would be increased by 160
     * for each such block, regardless of whether the block is transmitted in a
     * packet or dropped as silent.
     */
    protected int timestamp = 0;

    /**
     * Identifies the synchronization source. The value is chosen randomly, with
     * the intent that no two synchronization sources within the same RTP
     * session will have the same SSRC. Although the probability of multiple
     * sources choosing the same identifier is low, all RTP implementations must
     * be prepared to detect and resolve collisions. If a source changes its
     * source transport address, it must also choose a new SSRC to avoid being
     * interpreted as a looped source.
     */
    protected int SSRC = 0;

    /**
     * An array of 0 to 15 CSRC elements identifying the contributing sources
     * for the payload contained in this packet. The number of identifiers is
     * given by the CC field. If there are more than 15 contributing sources,
     * only 15 may be identified. CSRC identifiers are inserted by mixers, using
     * the SSRC identifiers of contributing sources. For example, for audio
     * packets the SSRC identifiers of all sources that were mixed together to
     * create a packet are listed, allowing correct talker indication at the
     * receiver.
     */
    protected int CSRC[] = new int[16];

    /**
     * The data payload of this packet.
     */
    protected byte[] payloadData = new byte[0];

    public RtpPacket(byte[] rtpPacket)
    {
        readBytes(rtpPacket);
    }

    public RtpPacket(int payloadType) throws IllegalArgumentException
    {
        setPayloadType(payloadType);
    }

    public RtpPacket()
    {

    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    /**
     * @return the padding
     */
    public boolean isPaddingSet()
    {
        return padding;
    }

    /**
     * @param padding the padding to set
     */
    public void setPadding(boolean padding)
    {
        this.padding = padding;
    }

    /**
     * @return the extension
     */
    public boolean isExtensionSet()
    {
        return extension;
    }

    public int getExtensionHeaderId()
    {
        return extensionHeaderId;
    }

    public byte[] getExtensionHeaderData()
    {
        return extensionHeaderData;
    }

    /**
     * @param extensionHeaderId id of the extension header
     * @param extensionHeaderData extension header data
     */
    public void setExtension(int extensionHeaderId, byte[] extensionHeaderData)
    {
        this.extension = true;
        this.extensionHeaderId = extensionHeaderId;

        // Calculate size of extension header data
        // so that it aligns with the 32 bit word
        // boundaries:
        if (extensionHeaderData.length % 4 != 0)
        {
            this.extensionHeaderData = new byte[extensionHeaderData.length +
                                                4 - extensionHeaderData.length % 4];
        }
        else
        {
            this.extensionHeaderData = new byte[extensionHeaderData.length];
        }

        // Copy the data to the internal array:
        System.arraycopy(extensionHeaderData, 0,
                         this.extensionHeaderData, 0,
                         extensionHeaderData.length);
    }

    /**
     * @return the cSRCCount
     */
    public int getCSRCCount()
    {
        return CSRCCount;
    }

    /**
     * @return the marker
     */
    public boolean isMarkerSet()
    {
        return marker;
    }

    /**
     * @param marker the marker to set
     */
    public void setMarker(boolean marker)
    {
        this.marker = marker;
    }

    /**
     * @return the payloadType
     */
    public int getPayloadType()
    {
        return payloadType;
    }

    /**
     * @param payloadType the payloadType to set
     */
    public void setPayloadType(int payloadType) throws IllegalArgumentException
    {
        if (payloadType < 0 || payloadType > 128)
        {
            throw new IllegalArgumentException("Payload type must be within 0 to 128.");
        }

        this.payloadType = payloadType;
    }

    /**
     * @return the sequenceNumber
     */
    public int getSequenceNumber()
    {
        return sequenceNumber;
    }

    /**
     * @param sequenceNumber the sequenceNumber to set
     *
     * @throws Exception if sequence number is greater
     * than 2^16-1 or less than 0
     */
    public void setSequenceNumber(int sequenceNumber) throws IllegalArgumentException
    {
        if (sequenceNumber < 0 || sequenceNumber > MAX_SEQUENCE_NUM)
        {
            throw new IllegalArgumentException("Sequence number must be within 0 to 65535");
        }

        this.sequenceNumber = sequenceNumber;
    }

    /**
     * @return the timestamp
     */
    public int getTimestamp()
    {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(int timestamp)
    {
        this.timestamp = timestamp;
    }

    /**
     * @return the sSRC
     */
    public int getSSRC()
    {
        return SSRC;
    }

    /**
     * @param ssrc the sSRC to set
     */
    public void setSSRC(int ssrc)
    {
        SSRC = ssrc;
    }

    /**
     * @return the cSRC
     */
    public int[] getCSRCs()
    {
        return CSRC;
    }

    /**
     * @param csrcArray the CSRC values to set
     */
    public void setCSRCs(int[] csrcArray) throws IllegalArgumentException
    {
        int count = csrcArray.length;

        if (count > 16)
        {
            throw new IllegalArgumentException("CSRC count must be with 0 and 16.");
        }

        CSRCCount = (byte)count;
        CSRC = csrcArray;
    }

    /**
     * @return the rtpData
     */
    public byte[] getRtpPayloadData()
    {
        return payloadData;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("[RTPPacket]\n");
        sb.append("Version: " + version + "\n");
        sb.append("Padding: " + padding + "\n");
        sb.append("Extension: " + extension + "\n");
        sb.append("CSRC Count: " + CSRCCount + "\n");
        sb.append("Marker: " + marker + "\n");
        sb.append("Payload Type: " + payloadType + "\n");
        sb.append("Sequence Number: " + sequenceNumber + "\n");
        sb.append("Timestamp: " + timestamp + "\n");
        sb.append("SSRC: " + SSRC + "\n");
        sb.append("Extension Header Id: " + extensionHeaderId + "\n");

        return sb.toString();
    }

    /**
     * @param rtpPayloadData the rtpData to set
     */
    public void setRtpPayloadData(byte[] rtpPayloadData)
    {
        this.payloadData = new byte[rtpPayloadData.length];

        System.arraycopy(rtpPayloadData, 0,
                         this.payloadData, 0,
                         rtpPayloadData.length);
    }

    public byte[] getBytes()
    {
        int totalHeaderSize = FIXED_HEADER_SIZE + CSRCCount * 4;

        if (extension)
        {
            // Increase size for header Id and length:
            totalHeaderSize += 4 + extensionHeaderData.length;
        }

        byte[] packet = new byte[totalHeaderSize + payloadData.length];

        byte P = (byte) (padding ? 1 : 0);
        byte X = (byte) (extension ? 1 : 0);
        byte M = (byte) (marker ? 1 : 0);

        packet[0] = (byte) (((version & 0x3) << 6) | ((P & 0x1) << 5)
                | ((X & 0x1) << 4) | ((CSRCCount & 0xf) << 0));
        packet[1] = (byte) (((M & 0x1) << 7) | ((payloadType & 0x7f) << 0));
        packet[2] = (byte) ((sequenceNumber & 0xff00) >> 8);
        packet[3] = (byte) ((sequenceNumber & 0x00ff) >> 0);

        /* Timestamp */
        byte[] ts = convertIntToBytes(timestamp);
        System.arraycopy(ts, 0, packet, 4, ts.length);

        /* SSRC */
        byte[] ssrc = convertIntToBytes(SSRC);
        System.arraycopy(ssrc, 0, packet, 8, ssrc.length);

        /* CSRCs (if any) */
        for (int i = 0; i < CSRCCount; i++)
        {
            byte[] csrc = convertIntToBytes(CSRC[i]);
            System.arraycopy(csrc, 0,
                             packet, FIXED_HEADER_SIZE + 4 * i,
                             csrc.length);
        }

        /* Extension Header */
        if (extension)
        {
            // Extension header identifier:
            packet[FIXED_HEADER_SIZE + CSRCCount * 4] = (byte)((extensionHeaderId & 0xFF00) >> 8);
            packet[FIXED_HEADER_SIZE + CSRCCount * 4 + 1] = (byte)(extensionHeaderId & 0x00FF);

            int length = extensionHeaderData.length / 4;
            packet[FIXED_HEADER_SIZE + CSRCCount * 4 + 2] = (byte)((length & 0xFF00) >> 8);
            packet[FIXED_HEADER_SIZE + CSRCCount * 4 + 3] = (byte)(length & 0x00FF);

            // Extension header data:
            System.arraycopy(extensionHeaderData, 0,
                             packet, FIXED_HEADER_SIZE + CSRCCount * 4 + 4,
                             extensionHeaderData.length);
        }

        /* RTP Payload Data */
        System.arraycopy(payloadData, 0, packet, totalHeaderSize, payloadData.length);

        return packet;
    }

    private byte[] convertIntToBytes(int number)
    {
        // Using big endian (network order):
        byte[] bytes = new byte[4];

        bytes[0] = (byte) ((number & 0xff000000) >>> 24);
        bytes[1] = (byte) ((number & 0x00ff0000) >>> 16);
        bytes[2] = (byte) ((number & 0x0000ff00) >>> 8);
        bytes[3] = (byte)  (number & 0x000000ff);

        return bytes;
    }

    private int convertFourBytesToInt(byte[] bytes, int offset)
    {
        int number = (bytes[offset] << 24) |
                     ((bytes[offset + 1] & 0xFF) << 16) |
                     ((bytes[offset + 2] & 0xFF) << 8) |
                     (bytes[offset + 3] & 0xFF);

        return number;
    }

    private void readBytes(byte[] rtpPacket)
    {
        version = (rtpPacket[0] & 0xC0) >>> 6;
        padding = (rtpPacket[0] & 0x20) != 0;
        extension = (rtpPacket[0] & 0x10) != 0;
        CSRCCount = rtpPacket[0] & 0x0F;

        marker = (rtpPacket[1] & 0x80) != 0;
        payloadType = rtpPacket[1] & 0x7F;

        sequenceNumber = ((rtpPacket[2] & 0xFF) << 8) | (rtpPacket[3] & 0xFF);

        timestamp = convertFourBytesToInt(rtpPacket, 4);
        SSRC = convertFourBytesToInt(rtpPacket, 8);

        int readIndex = 12;
        CSRC = new int[CSRCCount];
        for (int i = 0; i < CSRCCount; i++)
        {
            CSRC[i] = convertFourBytesToInt(rtpPacket, readIndex);
            readIndex += 4;
        }

        // Extension header:
        if (extension)
        {
            extensionHeaderId = (rtpPacket[readIndex] << 8) |
                                 rtpPacket[readIndex + 1];

            // Length specified the number of 32 bit words in the extension.
            int length = (rtpPacket[readIndex + 2] << 8) |
                          rtpPacket[readIndex + 3];

            extensionHeaderData = new byte[length * 4];

            // Copy the extension data to another array:
            System.arraycopy(rtpPacket, readIndex + 4,
                             extensionHeaderData, 0,
                             extensionHeaderData.length);

            readIndex += 4 + length * 4;
        }

        // Payload data - the remaining bytes in the packet are
        // the payload:
        payloadData = new byte[rtpPacket.length - readIndex];

        System.arraycopy(rtpPacket, readIndex, payloadData, 0, payloadData.length);
    }

    public static void main(String[] args) throws IllegalArgumentException
    {
        Random generator = new Random(19580427);

        byte[] rtpExtensionHeaderData = new byte[50];
        for (int i=0; i<50; i++)
        {
            rtpExtensionHeaderData[i] = (byte)(i%256);
        }

        byte[] rtpPayloadData = new byte[1000];
        for (int i=0; i<1000; i++)
        {
            rtpPayloadData[i] = (byte)(i%256);
        }

        RtpPacket sentPacket = new RtpPacket(101);
        sentPacket.setSequenceNumber(10008);
        sentPacket.setTimestamp(generator.nextInt());
        sentPacket.setSSRC(generator.nextInt());
        sentPacket.setCSRCs(new int[8]);
        sentPacket.setExtension(15, rtpExtensionHeaderData);
        sentPacket.setRtpPayloadData(rtpPayloadData);

        System.out.println(sentPacket.toString());

        byte[] fullPacket = sentPacket.getBytes();

//        for (int i=0; i<fullPacket.length/10; i+=4)
//        {
//            System.out.println(
//                    (int)fullPacket[i]+" "+
//                    (int)fullPacket[i+1]+" "+
//                    (int)fullPacket[i+2]+" "+
//                    (int)fullPacket[i+3]);
//        }

        RtpPacket receivedPacket = new RtpPacket(fullPacket);
        System.out.println(receivedPacket.toString());

        byte[] receivedExtensionHeaderData = receivedPacket.getExtensionHeaderData();
        byte[] receivedPayloadData = receivedPacket.getRtpPayloadData();

        if (rtpExtensionHeaderData.length <= receivedExtensionHeaderData.length)
        {
            for (int i = 0; i < rtpExtensionHeaderData.length; i++)
            {
                if (rtpExtensionHeaderData[i] != receivedExtensionHeaderData[i])
                {
                    System.out.println("Extension Header Data Doesn't Match");
                }
            }
        }

        if (rtpPayloadData.length == receivedPayloadData.length)
        {
            for (int i = 0; i < rtpPayloadData.length; i++)
            {
                if (rtpPayloadData[i] != receivedPayloadData[i])
                {
                    System.out.println("Payload Data Doesn't Match");
                }
            }
        }
    }
}
