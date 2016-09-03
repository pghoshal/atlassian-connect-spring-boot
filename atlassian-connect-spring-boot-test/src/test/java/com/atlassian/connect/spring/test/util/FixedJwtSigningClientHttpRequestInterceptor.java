package com.atlassian.connect.spring.test.util;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.request.jwt.JwtSigningClientHttpRequestInterceptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;

import java.net.URI;
import java.util.Optional;

public class FixedJwtSigningClientHttpRequestInterceptor extends JwtSigningClientHttpRequestInterceptor {

    private String jwt;

    public FixedJwtSigningClientHttpRequestInterceptor(String jwt) {
        this.jwt = jwt;
    }

    @Override
    protected Optional<AtlassianHostUser> getHostUserForRequest(HttpRequest request) {
        AtlassianHost host = new AtlassianHost();
        host.setBaseUrl(AtlassianHosts.BASE_URL);
        return Optional.of(new AtlassianHostUser(host, Optional.empty()));
    }

    @Override
    protected String createJwt(HttpMethod method, URI uri, AtlassianHostUser hostUser) {
        return jwt;
    }
}
