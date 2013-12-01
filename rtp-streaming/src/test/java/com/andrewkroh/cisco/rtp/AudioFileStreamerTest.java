package com.andrewkroh.cisco.rtp;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import com.andrewkroh.cicso.rtp.AudioFileStreamer;
import com.andrewkroh.cicso.rtp.AudioFileStreamer.EncodingType;
import com.andrewkroh.cicso.rtp.RtpSession;

/**
 * Test for {@link AudioFileStreamer}.
 *
 * @author akroh
 */
public class AudioFileStreamerTest
{
    private static final URL SONAR_8K_PCM_WAV =
            AudioFileStreamerTest.class.getResource("/wavs/sonar_8kHz_pcm.wav");

    private AudioFileStreamer streamer;

    private RtpSession mockRtpSession;

    @Before
    public void beforeTest() throws UnsupportedAudioFileException, IOException
    {
        mockRtpSession = mock(RtpSession.class);
        streamer = new AudioFileStreamer(SONAR_8K_PCM_WAV,
                                         EncodingType.ULAW,
                                         20,
                                         mockRtpSession);
    }

    @After
    public void afterTest()
    {
        if (streamer != null)
        {
            streamer.stopAsync();
        }
    }

    @Test
    public void constructor_withUlaw_initializes()
    {
        assertNotNull(streamer.getRtpSession());
        assertNotNull(streamer.getOutputFormat());
        assertNotNull(Whitebox.getInternalState(streamer, "sourceDataBuffer"));
        assertThat(streamer.getSourceUrl(), equalTo(SONAR_8K_PCM_WAV));
        assertThat(streamer.getOutputPacketLengthMs(), equalTo(20L));
        assertThat((Integer) Whitebox.getInternalState(streamer, "payloadSizeBytes"),
                greaterThan(0));
    }

    @Test
    public void constructor_withAlaw_noException()
            throws UnsupportedAudioFileException, IOException
    {
        new AudioFileStreamer(SONAR_8K_PCM_WAV,
                              EncodingType.ALAW,
                              20,
                              mockRtpSession);
    }

    @Test
    public void constructor_withPcm16_noException()
            throws UnsupportedAudioFileException, IOException
    {
        new AudioFileStreamer(SONAR_8K_PCM_WAV,
                              EncodingType.PCM16,
                              20,
                              mockRtpSession);
    }

    @Test(expected = NullPointerException.class)
    public void constructor_willNullUrl_throwsException()
            throws UnsupportedAudioFileException, IOException
    {
        new AudioFileStreamer(null, EncodingType.ALAW, 50, mockRtpSession);
    }

    @Test(expected = NullPointerException.class)
    public void constructor_willNullRtpSession_throwsException()
            throws UnsupportedAudioFileException, IOException
    {
        new AudioFileStreamer(SONAR_8K_PCM_WAV, EncodingType.ALAW, 50, null);
    }

    @Test(expected = NullPointerException.class)
    public void constructor_willNullEncodingType_throwsException()
            throws UnsupportedAudioFileException, IOException
    {
        new AudioFileStreamer(SONAR_8K_PCM_WAV, null, 50, mockRtpSession);
    }

    @Test
    public void sendData_isInvokedAtFixedInterval() throws TimeoutException
    {
        streamer.startAsync();
        streamer.awaitRunning(5, TimeUnit.SECONDS);
        verify(mockRtpSession, timeout(110).times(5)).sendData((byte[])any(), anyInt(), anyLong());
    }

    @Test
    public void getNumberOfSamplesPerTimePeriod_with8khz_20ms_returns160Samples()
    {
        getNumberOfSamplesPerTimePeriodTest(8000f, 20, 160);
    }

    @Test
    public void getNumberOfSamplesPerTimePeriod_with8khz_1000ms_returns8000Samples()
    {
        getNumberOfSamplesPerTimePeriodTest(8000f, 1000, 8000);
    }

    private void getNumberOfSamplesPerTimePeriodTest(float sampleRateHz, int timeMs, int expectedNumSamples)
    {
        AudioFormat mockAudioFormat = mock(AudioFormat.class);
        when(mockAudioFormat.getSampleRate()).thenReturn(sampleRateHz);

        assertThat(AudioFileStreamer.getNumberOfSamplesPerTimePeriod(
                mockAudioFormat, timeMs, TimeUnit.MILLISECONDS), equalTo(expectedNumSamples));
    }
}
