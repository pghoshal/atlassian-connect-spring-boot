package com.atlassian.connect.spring.internal.jwt;

/**
 * Indicates that the JWT's signature does not match its contents or the shared secret for the specified issuer.
 */
public class JwtSignatureMismatchException extends JwtVerificationException {

    public JwtSignatureMismatchException(Exception cause) {
        super(cause);
    }

    public JwtSignatureMismatchException(String reason) {
        super(reason);
    }
}
