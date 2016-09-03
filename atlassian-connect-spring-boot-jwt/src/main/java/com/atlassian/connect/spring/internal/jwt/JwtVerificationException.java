package com.atlassian.connect.spring.internal.jwt;

/**
 * Indicates a JWT was well-formed, but failed to validate.
 */
public abstract class JwtVerificationException extends Exception {

    protected JwtVerificationException(String message) {
        super(message);
    }

    protected JwtVerificationException(String message, Throwable cause) {
        super(message, cause);
    }

    protected JwtVerificationException(Throwable cause) {
        super(cause);
    }
}
