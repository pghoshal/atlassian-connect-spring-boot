package com.atlassian.connect.spring.internal.jwt;

/**
 * Thrown if a problem was encountered while signing a JWT.
 */
public class JwtSigningException extends RuntimeException {

    public JwtSigningException(Exception cause) {
        super(cause);
    }
}
