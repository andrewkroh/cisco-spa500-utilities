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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertArrayEquals;

import org.junit.Before;
import org.junit.Test;

import com.andrewkroh.cicso.rtp.RtpPacket;

/**
 * Test for {@link RtpPacket}.
 *
 * @author akroh
 */
public class RtpPacketTest
{
    private static final boolean MARKER = true;

    private static final boolean PADDING = true;

    private static final int PAYLOAD_TYPE = 103;

    private static final int SEQ_NUM = 103;

    private static final int TIMESTAMP = 211;

    private static final byte[] PAYLOAD_DATA = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

    private RtpPacket localPacket;

    private RtpPacket receivedPacket;

    @Before
    public void beforeTest()
    {
        localPacket = new RtpPacket();
        localPacket.setMarker(MARKER);
        localPacket.setPadding(PADDING);
        localPacket.setPayloadType(PAYLOAD_TYPE);
        localPacket.setSequenceNumber(SEQ_NUM);
        localPacket.setTimestamp(TIMESTAMP);
        localPacket.setRtpPayloadData(PAYLOAD_DATA);

        receivedPacket = new RtpPacket(localPacket.getBytes());
    }

    @Test
    public void receivedPacket_hasMatchingMarker()
    {
        assertThat(receivedPacket.isMarkerSet(), equalTo(MARKER));
    }

    @Test
    public void receivedPacket_hasMatchingPadding()
    {
        assertThat(receivedPacket.isPaddingSet(), equalTo(PADDING));
    }

    @Test
    public void receivedPcaket_hasMatchingPayloadType()
    {
        assertThat(receivedPacket.getPayloadType(), equalTo(PAYLOAD_TYPE));
    }

    @Test
    public void receivedPacket_hasMatchingSequenceNumber()
    {
        assertThat(receivedPacket.getSequenceNumber(), equalTo(SEQ_NUM));
    }

    @Test
    public void receivedPacket_hasMatchingTimestamp()
    {
        assertThat(receivedPacket.getTimestamp(), equalTo(TIMESTAMP));
    }

    @Test
    public void receivedPacket_hasMatchingPayloadData()
    {
        assertArrayEquals(receivedPacket.getRtpPayloadData(),
                          new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
    }

    @Test
    public void receivedPacket_hasMatchingCsrcCount()
    {
        assertThat(receivedPacket.getCSRCCount(), equalTo(0)); // TODO
    }

    @Test
    public void receivedPacket_hasEmptyCSRCs()
    {
        assertThat(receivedPacket.getCSRCs().length, equalTo(0)); // TODO
    }

    @Test
    public void receivedPacket_hasEmptyExtensionHeaderData()
    {
        assertThat(receivedPacket.getExtensionHeaderData().length, equalTo(0)); // TODO
    }
}
