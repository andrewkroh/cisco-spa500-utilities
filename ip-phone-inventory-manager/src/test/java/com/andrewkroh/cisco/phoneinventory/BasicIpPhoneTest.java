package com.andrewkroh.cisco.phoneinventory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

/**
 * Test for {@link BasicIpPhone}.
 *
 * @author akroh
 */
public class BasicIpPhoneTest
{
    private static final String HOST = "hostname";

    private static final Integer PORT = 80;

    private static final String USERNAME = "user";

    private static final String PASSWORD = "pass";

    private static final BasicIpPhone phone =
            new BasicIpPhone(HOST, PORT, USERNAME, PASSWORD);

    @Test(expected = NullPointerException.class)
    public void constructor_withNullHost_throwsException()
    {
        new BasicIpPhone(null, PORT, USERNAME, PASSWORD);
    }

    @Test(expected = NullPointerException.class)
    public void constructor_withNullPort_throwsException()
    {
        new BasicIpPhone(HOST, null, USERNAME, PASSWORD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_withNegativePort_throwsException()
    {
        new BasicIpPhone(HOST, -1, USERNAME, PASSWORD);
    }

    @Test(expected = NullPointerException.class)
    public void constructor_withNullUsername_throwsException()
    {
        new BasicIpPhone(HOST, PORT, null, PASSWORD);
    }

    @Test(expected = NullPointerException.class)
    public void constructor_withNullPassword_throwsException()
    {
        new BasicIpPhone(HOST, PORT, USERNAME, null);
    }

    @Test
    public void getHostname_returnsHost()
    {
        assertThat(phone.getHostname(), equalTo(HOST));
    }

    @Test
    public void getPort_returnsPort()
    {
        assertThat(phone.getPort(), equalTo(PORT));
    }

    @Test
    public void getUsername_returnsUsername()
    {
        assertThat(phone.getUsername(), equalTo(USERNAME));
    }

    @Test
    public void getPassword_returnsPassword()
    {
        assertThat(phone.getPassword(), equalTo(PASSWORD));
    }
}
