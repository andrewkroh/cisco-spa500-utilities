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

package com.andrewkroh.cisco.xmlservices;

import com.andrewkroh.cisco.phoneinventory.IpPhone;
import com.cisco.xmlservices.generated.CiscoIPPhoneExecute;
import com.cisco.xmlservices.generated.CiscoIPPhoneResponse;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Service;

/**
 * This represents XML service interface running on a Cisco IP Phones. It allows
 * users to submit (push) XML to the phone(s). This push is done via an HTTP
 * POST.
 *
 * <p/>
 * Typical usage of this command is for triggering an action by the phone, such
 * as start streaming audio data, start receiving audio data, or display a
 * menu/prompt. In order to display a menu you submit the URL of the menu to the
 * phone and it makes a HTTP GET request for that URL.
 *
 * <p/>
 * According to the documentation the maximum supported HTTP POST is limited to
 * 512 bytes. Larger objects (such as images) can only be delivered to the phone
 * via HTTP GET. So, to push large objects to the phone, the server application
 * must take an indirect approach. To do this, push an Execute object to the
 * phone that contains an ExecuteItem pointing to the URL of the large object.
 *
 * @author akroh
 */
public interface XmlPushService extends Service
{
    /**
     * Submits a command to a single phone.
     *
     * @param phone
     *            {@link IpPhone} to send the command to
     * @param command
     *            command to send
     * @return {@link ListenableFuture} that will return a
     *         {@link XmlPushResponse} containing the phone's
     *         {@link CiscoIPPhoneResponse}. The future will automatically
     *         timeout if no response is received.
     */
    ListenableFuture<XmlPushResponse> submitCommand(
            IpPhone phone,
            CiscoIPPhoneExecute command);

    /**
     * Submits a command that expects a callback to a single phone.
     *
     * @param phone
     *            {@link IpPhone} to send the command to
     * @param command
     *            command to send
     * @param commandCallback
     *            {@link XmlPushCallback} that hosts the callback URLs, if
     *            {@code commandCallback} is not already registered as a
     *            callback it will be registered and have its
     *            {@link XmlPushCallback#setCallbackUrl(String)} method invoked.
     * @return {@link ListenableFuture} that will return a
     *         {@link XmlPushResponse} containing the phone's
     *         {@link CiscoIPPhoneResponse}. The future will automatically
     *         timeout if no response is received.
     */
    ListenableFuture<XmlPushResponse> submitCommand(
            IpPhone phone,
            CiscoIPPhoneExecute command,
            XmlPushCallback commandCallback);

    /**
     * Submits a command to multiple IP phones.
     *
     * @param phones
     *            {@link IpPhone}s to send the command to
     * @param command
     *            command to send
     * @return list of {@link ListenableFuture}s that will return a
     *         {@link XmlPushResponse} containing the phone's
     *         {@link CiscoIPPhoneResponse}. The future will automatically
     *         timeout if no response is received.
     */
    ImmutableList<ListenableFuture<XmlPushResponse>> submitCommand(
            ImmutableList<IpPhone> phones,
            CiscoIPPhoneExecute command);

    /**
     * Submits a command that expects a callback to a single phone.
     *
     * @param phones
     *            {@link IpPhone}s to send the command to
     * @param command
     *            command to send
     * @param commandCallback
     *            {@link XmlPushCallback} that hosts the callback URLs, if
     *            {@code commandCallback} is not already registered as a
     *            callback it will be registered and have its
     *            {@link XmlPushCallback#setCallbackUrl(String)} method invoked.
     * @return list of {@link ListenableFuture}s that will return a
     *         {@link XmlPushResponse} containing the phone's
     *         {@link CiscoIPPhoneResponse}. The future will automatically
     *         timeout if no response is received.
     */
    ImmutableList<ListenableFuture<XmlPushResponse>> submitCommand(
            ImmutableList<IpPhone> phones,
            CiscoIPPhoneExecute command,
            XmlPushCallback commandCallback);

    /**
     * Unregisters a {@link XmlPushCallback}.
     *
     * @param commandCallback
     *            XmlPushCallback to unregister, {@code null} is safely handled.
     * @return true if {@code commandCallback} has been unregistered
     */
    boolean unregisterCallback(XmlPushCallback commandCallback);
}
