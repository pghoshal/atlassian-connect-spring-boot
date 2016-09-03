package com.atlassian.connect.spring.internal.request.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static java.util.Collections.singletonList;

/**
 * A {@link RestTemplate} that signs requests to Atlassian hosts with JSON Web Tokens.
 *
 * Requests to absolute URLs are signed with the installation credentials of the Atlassian host with the base URL
 * matching the request URL. Requests to relative URLs are signed with the installation credentials of
 * {@link SecurityContextHolder the authenticated host}.
 */
@Component
public class JwtSigningRestTemplate extends RestTemplate {

    @Autowired
    public JwtSigningRestTemplate(
            JwtSigningClientHttpRequestInterceptor requestInterceptor) {
        setInterceptors(singletonList(requestInterceptor));
    }
}
