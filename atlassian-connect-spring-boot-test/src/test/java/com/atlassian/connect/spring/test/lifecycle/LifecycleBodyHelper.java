package com.atlassian.connect.spring.test.lifecycle;

import com.atlassian.connect.spring.test.util.AtlassianHosts;
import com.google.common.collect.ImmutableMap;

public class LifecycleBodyHelper {

    public static ImmutableMap<String, Object> createLifecycleEventMap(String eventType) {
        return createLifecycleEventMap(eventType, AtlassianHosts.SHARED_SECRET);
    }
    
    public static ImmutableMap<String, Object> createLifecycleEventMap(String eventType, String secret) {
        ImmutableMap.Builder<String, Object> mapBuilder = ImmutableMap.<String, Object>builder()
                .put("key", "atlassian-connect-spring-boot-test")
                .put("clientKey", AtlassianHosts.CLIENT_KEY)
                .put("publicKey", "MIGf....ZRWzwIDAQAB")
                .put("serverVersion", "server-version")
                .put("pluginsVersion", "version-of-connect")
                .put("baseUrl", AtlassianHosts.BASE_URL)
                .put("productType", "jira")
                .put("description", "Atlassian JIRA at https://example.atlassian.net")
                .put("serviceEntitlementNumber", "SEN-number")
                .put("eventType", eventType);
        if (eventType.equals("installed")) {
            mapBuilder.put("sharedSecret", secret);
        }
        return mapBuilder.build();
    }
}
