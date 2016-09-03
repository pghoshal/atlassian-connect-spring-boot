package com.atlassian.connect.spring.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for Atlassian Connect add-ons.
 */
@ConfigurationProperties(prefix = "atlassian.connect")
public class AtlassianConnectProperties {

    /**
     * Accept installations signed by an unknown host. (Useful in development mode using an in-memory database.)
     */
    private boolean allowReinstallMissingHost = false;

    /**
     * Enable debug mode for the JavaScript API, loading all-debug.js instead of all.js.
     */
    private boolean debugAllJs = false;

    public boolean isAllowReinstallMissingHost() {
        return allowReinstallMissingHost;
    }

    public void setAllowReinstallMissingHost(boolean allowReinstallMissingHost) {
        this.allowReinstallMissingHost = allowReinstallMissingHost;
    }

    public boolean isDebugAllJs() {
        return debugAllJs;
    }

    public void setDebugAllJs(boolean debugAllJs) {
        this.debugAllJs = debugAllJs;
    }
}
