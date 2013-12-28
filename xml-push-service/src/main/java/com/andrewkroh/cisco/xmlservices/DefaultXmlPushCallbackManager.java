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

@Default
@Singleton
public class DefaultXmlPushCallbackManager implements XmlPushCallbackManager
{
    private final ReentrantLock lock = new ReentrantLock();

    private final String baseCallbackUrl;

    private final BiMap<String, XmlPushCallback> xmlPushCallbacks;

    @Inject
    DefaultXmlPushCallbackManager(@Named("baseCallbackUrl") String baseCallbackUrl)
    {
        this.baseCallbackUrl = checkNotNull(baseCallbackUrl,
                "Base callback URL cannot be null.");
        checkArgument(!baseCallbackUrl.endsWith("/"),
                "Base callback URL cannot end with '/'");
        xmlPushCallbacks = HashBiMap.<String, XmlPushCallback>create();
    }

    @Override
    public Object getCallback(String id)
    {
        lock.lock();
        try
        {
            return xmlPushCallbacks.get(id);
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public String registerCallback(XmlPushCallback commandCallback)
    {
        checkNotNull(commandCallback, "XmlPushCallback cannot be null.");

        lock.lock();
        try
        {
            String callbackId =
                    xmlPushCallbacks.inverse().get(commandCallback);

            if (callbackId == null)
            {
                callbackId = UUID.randomUUID().toString();
                commandCallback.setCallbackId(callbackId);
                xmlPushCallbacks.put(callbackId, commandCallback);
            }

            return baseCallbackUrl + "/" + callbackId;
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public boolean unregisterCallback(XmlPushCallback commandCallback)
    {
        checkNotNull(commandCallback, "XmlPushCallback cannot be null.");

        lock.lock();
        try
        {
            return xmlPushCallbacks.inverse().remove(
                    commandCallback) != null ? true : false;
        }
        finally
        {
            lock.unlock();
        }
    }
}
