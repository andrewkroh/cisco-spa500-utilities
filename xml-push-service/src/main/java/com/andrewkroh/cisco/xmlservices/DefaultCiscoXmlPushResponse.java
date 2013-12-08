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

import com.cisco.xmlservices.generated.CiscoIPPhoneResponse;

/**
 *
 * @author akroh
 */
class DefaultCiscoXmlPushResponse implements CiscoXmlPushResponse
{
    private final CiscoIpPhone phone;

    private final CiscoIPPhoneResponse response;

    /**
     * @param phone
     * @param response
     */
    DefaultCiscoXmlPushResponse(CiscoIpPhone phone, CiscoIPPhoneResponse response)
    {
        this.phone = phone;
        this.response = response;
    }

    /**
     * @return the phone
     */
    public CiscoIpPhone getPhone()
    {
        return phone;
    }

    /**
     * @return the response
     */
    public CiscoIPPhoneResponse getResponse()
    {
        return response;
    }
}
