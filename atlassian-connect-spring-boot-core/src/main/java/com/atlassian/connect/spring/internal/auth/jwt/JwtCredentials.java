package com.atlassian.connect.spring.internal.auth.jwt;

import com.atlassian.connect.spring.internal.jwt.CanonicalHttpRequest;

/**
 * Authentication credentials representing an unverified JSON Web Token.
 */
public class JwtCredentials {

    private final String rawJwt;

    private final CanonicalHttpRequest canonicalHttpRequest;

    public JwtCredentials(String rawJwt, CanonicalHttpRequest canonicalHttpRequest) {
        this.rawJwt = rawJwt;
        this.canonicalHttpRequest = canonicalHttpRequest;
    }

    public CanonicalHttpRequest getCanonicalHttpRequest() {
        return canonicalHttpRequest;
    }

    public String getRawJwt() {
        return rawJwt;
    }
}
