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
import com.cisco.xmlservices.generated.CiscoIPPhoneResponse;

/**
 * Interface for responses to XML push requests to a phone.
 *
 * @author akroh
 */
public interface XmlPushResponse
{
    /**
     * Gets the phone that generated the response.
     *
     * @return the phone that generated the response
     */
    IpPhone getPhone();

    /**
     * The response object from the phone.
     *
     * @return the response object from the phone
     */
    CiscoIPPhoneResponse getResponse();
}
