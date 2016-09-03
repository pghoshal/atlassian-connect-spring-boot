package com.atlassian.connect.spring.test.util;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.request.jwt.JwtBuilder;
import com.atlassian.connect.spring.internal.request.jwt.JwtSigningClientHttpRequestInterceptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;

import java.net.URI;
import java.util.Optional;

public class SimpleJwtSigningClientHttpRequestInterceptor extends JwtSigningClientHttpRequestInterceptor {

    private final String clientKey;
    private final String sharedSecret;
    private final Optional<String> optionalSubject;

    public SimpleJwtSigningClientHttpRequestInterceptor(String clientKey, String sharedSecret, Optional<String> optionalSubject) {
        this.clientKey = clientKey;
        this.sharedSecret = sharedSecret;
        this.optionalSubject = optionalSubject;
    }

    @Override
    protected Optional<AtlassianHostUser> getHostUserForRequest(HttpRequest request) {
        AtlassianHost host = new AtlassianHost();
        host.setBaseUrl(AtlassianHosts.BASE_URL);
        return Optional.of(new AtlassianHostUser(host, Optional.empty()));
    }

    @Override
    public String createJwt(HttpMethod method, URI uri, AtlassianHostUser hostUser) {
        JwtBuilder jwtBuilder = new JwtBuilder()
                .issuer(clientKey)
                .queryHash(method, uri, getBaseUrl(uri))
                .signature(sharedSecret);
        optionalSubject.ifPresent(jwtBuilder::subject);
        return jwtBuilder.build();
    }
}
