package com.andrewkroh.cisco.common;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.andrewkroh.cisco.common.HexUtility;

/**
 * Unit test for {@link HexUtility}.
 *
 * @author akroh
 */
public class HexUtilityTest
{
    private static final byte[] TEST_ARRAY =
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

    @Test
    public void bytesToHex_wholeArray_mapsValuesToCorrectHexRepresentation()
    {
        assertThat(HexUtility.bytesToHex(TEST_ARRAY),
                equalTo("000102030405060708090A0B0C0D0E0F"));
    }

    @Test
    public void bytesToHex_wholeArrayUsingIndexes_mapsValuesToCorrectHexRepresentation()
    {
        assertThat(HexUtility.bytesToHex(TEST_ARRAY, 0, 15),
                equalTo("000102030405060708090A0B0C0D0E0F"));
    }

    @Test
    public void bytesToHex_equalToAndFromIndex_returnsSingleHexValue()
    {
        assertThat(HexUtility.bytesToHex(TEST_ARRAY, 1, 1),
                equalTo("01"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void bytesToHex_invalidToIndex_throwsException()
    {
        HexUtility.bytesToHex(TEST_ARRAY, -1, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void bytesToHex_invalidFromIndex_throwsException()
    {
        HexUtility.bytesToHex(TEST_ARRAY, 1, 20);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void bytesToHex_fromIndexLessThanToIndex_throwsException()
    {
        HexUtility.bytesToHex(TEST_ARRAY, 3, 1);
    }

    @Test(expected = NullPointerException.class)
    public void bytesToHex_nullArray_throwsException()
    {
        HexUtility.bytesToHex(null, 0, 1);
    }
}
