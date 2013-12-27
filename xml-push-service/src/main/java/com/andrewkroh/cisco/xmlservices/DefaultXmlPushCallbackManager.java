package com.andrewkroh.cisco.xmlservices;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import javax.enterprise.inject.Default;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

@Default
public class DefaultXmlPushCallbackManager implements XmlPushCallbackManager
{
    private final ReentrantLock lock = new ReentrantLock();

    private final BiMap<String, XmlPushCallback> xmlPushCallbacks;

    DefaultXmlPushCallbackManager()
    {
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

            return callbackId;
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
