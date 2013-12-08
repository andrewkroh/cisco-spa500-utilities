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

import java.util.List;
import java.util.concurrent.Future;

import com.cisco.xmlservices.generated.CiscoIPPhoneExecute;
import com.cisco.xmlservices.generated.CiscoIPPhoneResponse;

/**
 * This represents XML service interface running on a Cisco IP Phones. It allows
 * users to submit (push) XML to the phone(s). This push is done via an HTTP
 * POST.
 *
 * <p/>
 * Typical usage of this command is to trigger an action by the phone, such as
 * start streaming audio data, start receiving audio data, or display a
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
public interface CiscoXmlPushService
{
    Future<CiscoIPPhoneResponse> submitCommand(CiscoIpPhone phone,
                                               CiscoIPPhoneExecute command);

    List<Future<CiscoIPPhoneResponse>> submitCommand(List<CiscoIpPhone> phones,
                                                     CiscoIPPhoneExecute command);
}
