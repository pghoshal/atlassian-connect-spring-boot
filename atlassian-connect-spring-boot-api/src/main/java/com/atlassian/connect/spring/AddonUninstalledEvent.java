package com.atlassian.connect.spring;

import org.springframework.context.ApplicationEvent;

/**
 * A Spring application event fired when the add-on has been successfully uninstalled from a host.
 *
 * <strong>NOTE:</strong> This event is fired asynchronously and cannot affect the HTTP response returned to the
 * Atlassian host.
 */
public class AddonUninstalledEvent extends ApplicationEvent {

    private final AtlassianHost host;

    /**
     * Creates a new event.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     * @param host the host for which the event occurred
     */
    public AddonUninstalledEvent(Object source, AtlassianHost host) {
        super(source);
        this.host = host;
    }

    /**
     * Returns the host for which the event occurred.
     *
     * @return the Atlassian host
     */
    public AtlassianHost getHost() {
        return host;
    }
}
