package com.atlassian.connect.spring.internal.jwt;

import java.util.Date;

/**
 * Thrown if the JWT's timestamps show that it has expired.
 */
public class JwtExpiredException extends JwtVerificationException {

    public JwtExpiredException(Date expiredAt, Date now, final int leewaySeconds) {
        super(String.format("Expired at %s and time is now %s (%d seconds leeway is allowed)", expiredAt, now, leewaySeconds));
    }
}
