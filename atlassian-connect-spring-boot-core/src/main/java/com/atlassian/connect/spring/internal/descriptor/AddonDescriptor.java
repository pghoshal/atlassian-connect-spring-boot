package com.atlassian.connect.spring.internal.descriptor;

import java.util.Optional;
import java.util.function.Function;

/**
 * An Atlassian Connect add-on descriptor (<code>atlassian-connect.json</code>).
 */
public class AddonDescriptor {

    private String key;

    private String baseUrl;

    private Authentication authentication;

    private Lifecycle lifecycle;

    public String getKey() {
        return key;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getAuthenticationType() {
        return Optional.ofNullable(authentication).map(Authentication::getType).orElse(null);
    }

    public String getInstalledLifecycleUrl() {
        return getLifecycleUrl(Lifecycle::getInstalled);
    }

    public String getUninstalledLifecycleUrl() {
        return getLifecycleUrl(Lifecycle::getUninstalled);
    }

    public String getEnabledLifecycleUrl() {
        return getLifecycleUrl(Lifecycle::getEnabled);
    }

    public String getDisabledLifecycleUrl() {
        return getLifecycleUrl(Lifecycle::getDisabled);
    }

    private String getLifecycleUrl(Function<Lifecycle, String> urlRetriever) {
        return Optional.ofNullable(lifecycle).map(urlRetriever).orElse(null);
    }

    private static class Authentication {

        private String type;

        public String getType() {
            return type;
        }
    }

    private static class Lifecycle {

        private String installed;

        private String uninstalled;

        private String enabled;

        private String disabled;

        public String getInstalled() {
            return installed;
        }

        public String getUninstalled() {
            return uninstalled;
        }

        public String getEnabled() {
            return enabled;
        }

        public String getDisabled() {
            return disabled;
        }
    }
}
