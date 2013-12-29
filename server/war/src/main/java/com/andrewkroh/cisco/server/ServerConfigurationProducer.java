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

package com.andrewkroh.cisco.server;

import static com.google.common.base.Preconditions.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Produces configurable values that are required by other classes during
 * CDI injection.
 *
 * @author akroh
 */
public class ServerConfigurationProducer
{
    /**
     * SLF4J logger for this class.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ServerConfigurationProducer.class);

    /**
     * Directory where configuration files will be loaded from. You can specify
     * this directory by setting the phone.config.dir system property.
     */
    private static final String CONFIG_DIR;

    static {
        String tmpConfigDir = System.getProperty("phone.config.dir");

        if (tmpConfigDir == null)
        {
            String jbossHome = System.getenv("JBOSS_HOME");

            if (jbossHome != null)
            {
                tmpConfigDir = jbossHome + File.separator + "config";
            }
            else
            {
                throw new IllegalArgumentException(
                        "JBOSS_HOME environment variable is not set. " +
                        "Either define JBOSS_HOME or use the phone.config.dir " +
                        "system property to configure the location of your " +
                        "config files.");
            }
        }

        if (new File(tmpConfigDir).exists())
        {
            LOGGER.info("Config files will be loaded from {}.", tmpConfigDir);
            CONFIG_DIR = tmpConfigDir;
        }
        else
        {
            throw new IllegalArgumentException("Configuration directory " +
                    tmpConfigDir + " does not exist.");
        }
    }

    /**
     * Provides the URL to the XML file that contains the inventory of all IP
     * phones.
     *
     * @return URL to an XML file containing inventory of all IP phones
     * @throws MalformedURLException
     *             if an error occurred while constructing the URL
     */
    @Produces
    @Named("phoneInventoryXml")
    public static URL getPhoneInventoryXml() throws MalformedURLException
    {
        return new File(CONFIG_DIR, "phone-inventory.xml").toURI().toURL();
    }

    /**
     * Provides the URL to the XML file containing the phone directory (users
     * and their phone numbers).
     *
     * @return URL to an XML file containing a phone directory
     * @throws MalformedURLException
     *             if an error occurred while constructing the URL
     */
    @Produces
    @Named("directoryXml")
    public static URL getDirectoryXml() throws MalformedURLException
    {
        return new File(CONFIG_DIR, "directory.xml").toURI().toURL();
    }

    /**
     * Produces the base callback URL. This is the URL to the
     * XmlPushCallbackRestResource that phones will use to retrieve information
     * after a message has been pushed. It should be similar to
     * http://[host]:[port]/phones/rest/callback.
     *
     * @return base URL that phones will use to callback to the server
     */
    @Produces
    @Named("baseCallbackUrl")
    public static String getBaseCallbackUrl()
    {
        return checkNotNull(System.getProperty("callback.url"),
                    "callback.url property must be set.");
    }
}
