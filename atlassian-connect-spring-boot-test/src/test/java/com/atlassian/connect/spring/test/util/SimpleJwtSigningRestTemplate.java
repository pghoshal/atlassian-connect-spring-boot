package com.atlassian.connect.spring.test.util;

import com.atlassian.connect.spring.AtlassianHost;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import java.util.Optional;

import static java.util.Collections.singletonList;

public class SimpleJwtSigningRestTemplate extends TestRestTemplate {

    public SimpleJwtSigningRestTemplate(String jwt) {
        this(new FixedJwtSigningClientHttpRequestInterceptor(jwt));
    }

    public SimpleJwtSigningRestTemplate(AtlassianHost host, Optional<String> optionalSubject) {
        this(host.getClientKey(), host.getSharedSecret(), optionalSubject);
    }

    public SimpleJwtSigningRestTemplate(String clientKey, String sharedSecret, Optional<String> optionalSubject) {
        this(new SimpleJwtSigningClientHttpRequestInterceptor(clientKey, sharedSecret, optionalSubject));
    }

    protected SimpleJwtSigningRestTemplate(ClientHttpRequestInterceptor requestInterceptor) {
        setInterceptors(singletonList(requestInterceptor));
    }
}
