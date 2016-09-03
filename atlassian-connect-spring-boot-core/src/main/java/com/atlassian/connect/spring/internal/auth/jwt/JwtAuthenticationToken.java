package com.atlassian.connect.spring.internal.auth.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

/**
 * An authentication token for a set of unverified {@link JwtCredentials JSON Web Token credentials}.
 *
 * @see JwtAuthentication
 * @see JwtCredentials
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private JwtCredentials jwtCredentials;

    public JwtAuthenticationToken(JwtCredentials jwtCredentials) {
        super(Collections.emptySet());
        this.jwtCredentials = jwtCredentials;
    }

    @Override
    public JwtCredentials getCredentials() {
        return jwtCredentials;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }
}
