package com.atlassian.connect.spring.internal.jwt;

/**
 * Indicates that the JWT was not well-formed, e.g. the JWT JSON is invalid.
 */
public class JwtParseException extends Exception {

    public JwtParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public JwtParseException(Exception cause) {
        super(cause);
    }

    public JwtParseException(String reason) {
        super(reason);
    }
}
