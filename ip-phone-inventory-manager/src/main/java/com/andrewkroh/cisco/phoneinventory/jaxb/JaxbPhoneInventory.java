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

package com.andrewkroh.cisco.phoneinventory.jaxb;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.common.collect.ImmutableList;

/**
 * Container class for marshaling and unmarshaling a list of
 * {@link JaxbIpPhone} objects to and read from XML using JAXB.
 *
 * @author akroh
 */
@XmlRootElement(name = "PhoneInventory")
public class JaxbPhoneInventory
{
    @XmlElement(name = "phone", nillable = false, required = true)
    private final List<JaxbIpPhone> phones;

    public JaxbPhoneInventory(List<JaxbIpPhone> phones)
    {
        this.phones = checkNotNull(phones);
    }

    @SuppressWarnings("unused")
    private JaxbPhoneInventory()
    {
        this(new ArrayList<JaxbIpPhone>());
    }

    public ImmutableList<JaxbIpPhone> getPhones()
    {
        return ImmutableList.copyOf(phones);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                    .append("phones", phones)
                    .build();
    }
}
