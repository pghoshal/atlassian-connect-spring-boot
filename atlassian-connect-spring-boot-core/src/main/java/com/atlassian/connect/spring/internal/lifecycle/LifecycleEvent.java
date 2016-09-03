package com.atlassian.connect.spring.internal.lifecycle;

import javax.validation.constraints.NotNull;

/**
 * The HTTP POST body of Atlassian Connect add-on lifecycle events.
 */
public class LifecycleEvent {

    @NotNull
    public String eventType;

    @NotNull
    public String key;

    @NotNull
    public String clientKey;

    public String publicKey;

    public String sharedSecret;

    public String serverVersion;

    public String pluginsVersion;

    @NotNull
    public String baseUrl;

    @NotNull
    public String productType;

    public String description;

    public String serviceEntitlementNumber;
}
