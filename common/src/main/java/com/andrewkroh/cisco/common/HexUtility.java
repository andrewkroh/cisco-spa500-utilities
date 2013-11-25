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

package com.andrewkroh.cisco.common;

import com.google.common.base.Preconditions;

/**
 * Utilities methods for converting bytes to hex.
 *
 * @author akroh
 */
public final class HexUtility
{
    /**
     * The sixteen hex characters an an array. The character's
     * index corresponds to it's base 10 value.
     */
    private static final char[] HEX_CHARACTERS =
            "0123456789ABCDEF".toCharArray();

    /**
     * Converts the given byte array to its hex representation.
     *
     * @param bytes
     *            byte array to convert to a string
     * @return a string containing each byte's hex value
     *
     * @throws NullPointerException
     *             if {@code bytes} is null
     */
    public static String bytesToHex(byte[] bytes)
    {
        return bytesToHex(bytes, 0, bytes.length - 1);
    }

    /**
     * Converts the specified range to elements from the given array to their
     * hex representation.
     *
     * @param bytes
     *            byte array containing elements to convert to a string
     * @param from
     *            index of the first element to convert to hex
     * @param to
     *            index of the last element to convert
     * @return a string containing the species range of bytes as hex
     *
     * @throws NullPointerException
     *             if {@code bytes} is null
     * @throws IndexOutOfBoundsException
     *             if either {@code from} index or {@code to} index is negative
     *             or is greater than size, or if {@code to} index is less than
     *             {@code from}
     */
    public static String bytesToHex(byte[] bytes, int from, int to)
    {
        Preconditions.checkNotNull(bytes);
        Preconditions.checkPositionIndexes(from, to, bytes.length);

        int numElements = to - from + 1;
        char[] hexChars = new char[numElements * 2];

        for ( int j = 0; j < numElements; j++ ) {
            int value = bytes[from + j] & 0xFF;
            hexChars[j * 2] = HEX_CHARACTERS[value >>> 4];
            hexChars[j * 2 + 1] = HEX_CHARACTERS[value & 0x0F];
        }

        return new String(hexChars);
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private HexUtility()
    {
        // Not to be instantiated.
    }
}
