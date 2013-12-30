package com.andrewkroh.cisco.xmlservices;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Manages {@link XmlPushCallback}s that are available to receive callbacks from
 * IP phones. Once a callback has been registered additional calls to register
 * it will have no effect. A callback should be explicitly unregistered after it
 * is no longer in use.
 *
 * <p/>
 * This class is thread-safe.
 *
 * @author akroh
 */
@Default
@Singleton
public class DefaultXmlPushCallbackManager implements XmlPushCallbackManager
{
    /**
     * Base URL where registered callbacks will be published. For example if the
     * base URL is {@code http://server:8080/phones/rest/callback} then a
     * callback would be published at
     * {@code http://server:8080/phones/rest/callback/random-uuid}.
     */
    private final String baseCallbackUrl;

    /**
     * Lock that prevents concurrent access to {@link #xmlPushCallbacks}.
     */
    private final ReentrantLock xmlPushCallbacksLock = new ReentrantLock();

    /**
     * Bidirectional map of callback ID (UUID) to {@link XmlPushCallback}.
     */
    private final BiMap<String, XmlPushCallback> xmlPushCallbacks =
            HashBiMap.<String, XmlPushCallback>create();

    /**
     * Creates a new {@code DefaultXmlPushCallbackManager} and initializes it
     * with the base callback URL.
     *
     * @param baseCallbackUrl
     *            base URL where callback will be published, it is the URL to
     *            {@link XmlPushCallbackRestResource}. It is specified manually
     *            to allow a reverse proxy to be used.
     */
    @Inject
    DefaultXmlPushCallbackManager(@Named("baseCallbackUrl") String baseCallbackUrl)
    {
        this.baseCallbackUrl = checkNotNull(baseCallbackUrl,
                "Base callback URL cannot be null.");
        checkArgument(!baseCallbackUrl.endsWith("/"),
                "Base callback URL cannot end with '/'");
    }

    @Override
    public XmlPushCallback getCallback(String callbackId)
    {
        checkNotNull(callbackId, "Callback ID cannot be null.");

        xmlPushCallbacksLock.lock();
        try
        {
            return xmlPushCallbacks.get(callbackId);
        }
        finally
        {
            xmlPushCallbacksLock.unlock();
        }
    }

    @Override
    public String registerCallback(XmlPushCallback commandCallback)
    {
        checkNotNull(commandCallback, "XmlPushCallback cannot be null.");

        xmlPushCallbacksLock.lock();
        try
        {
            String callbackId =
                    xmlPushCallbacks.inverse().get(commandCallback);

            if (callbackId == null)
            {
                callbackId = UUID.randomUUID().toString();
                commandCallback.setCallbackUrl(
                        baseCallbackUrl + "/" + callbackId);
                xmlPushCallbacks.put(callbackId, commandCallback);
            }

            return baseCallbackUrl + "/" + callbackId;
        }
        finally
        {
            xmlPushCallbacksLock.unlock();
        }
    }

    @Override
    public boolean unregisterCallback(XmlPushCallback commandCallback)
    {
        if (commandCallback == null)
        {
            return false;
        }

        xmlPushCallbacksLock.lock();
        try
        {
            return xmlPushCallbacks.inverse().remove(
                    commandCallback) != null ? true : false;
        }
        finally
        {
            xmlPushCallbacksLock.unlock();
        }
    }
}
