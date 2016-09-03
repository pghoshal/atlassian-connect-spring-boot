package com.atlassian.connect.spring.internal.jwt;

/**
 * Thrown if an expected claim is missing or the value of a reserved claim did not match its expected format.
 */
public class JwtInvalidClaimException extends JwtVerificationException {

    public JwtInvalidClaimException(String message) {
        super(message);
    }

    public JwtInvalidClaimException(String message, Throwable cause) {
        super(message, cause);
    }
}
