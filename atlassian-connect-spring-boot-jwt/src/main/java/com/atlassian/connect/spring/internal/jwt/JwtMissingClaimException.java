package com.atlassian.connect.spring.internal.jwt;

public class JwtMissingClaimException extends JwtParseException {

    public JwtMissingClaimException(String reason) {
        super(reason);
    }
}
