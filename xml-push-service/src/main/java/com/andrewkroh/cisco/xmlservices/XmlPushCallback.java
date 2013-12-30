package com.andrewkroh.cisco.xmlservices;

import javax.validation.constraints.NotNull;

/**
 * Marker interface for classes that should be hosted as a JAX-RS
 * resource to receive callbacks from IP phones. A {@link XmlPushCallback}
 * will accompany an outgoing "push command" that needs callbacks.
 *
 * <p/>
 * The implementation will be hosted as JAX-RS sub-resource when it is
 * submitted with an outgoing command. The callback resource will remain
 * registered until it is explicitly unregistered.
 *
 * @author akroh
 */
public interface XmlPushCallback
{
    /**
     * Sets the callback URL associated with this class. This information may be
     * needed to return additional URLs in messages to phones.
     *
     * <p/>
     * This method will only be invoked when the callback is registered.
     *
     * @param callbackUrl
     *            full URL to this resource, includes the callbackId
     */
    void setCallbackUrl(@NotNull String callbackUrl);
}
