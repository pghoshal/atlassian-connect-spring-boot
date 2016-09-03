package com.atlassian.connect.spring.test.util;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;

public final class AtlassianHosts {

    public static final String CLIENT_KEY = "some-host";

    public static final String SHARED_SECRET = "some-secret";

    public static final String BASE_URL = "http://example.com/product";

    private AtlassianHosts() {}

    public static AtlassianHost createAndSaveHost(AtlassianHostRepository hostRepository) {
        AtlassianHost host = createHost();
        hostRepository.save(host);
        return host;
    }

    public static AtlassianHost createHost() {
        AtlassianHost host = new AtlassianHost();
        host.setClientKey(CLIENT_KEY);
        host.setSharedSecret(SHARED_SECRET);
        host.setBaseUrl(BASE_URL);
        host.setProductType("some-product");
        return host;
    }
}
