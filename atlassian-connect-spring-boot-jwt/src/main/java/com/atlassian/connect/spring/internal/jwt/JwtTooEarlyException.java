package com.atlassian.connect.spring.internal.jwt;

import java.util.Date;

public class JwtTooEarlyException extends JwtVerificationException {

    public JwtTooEarlyException(Date notBefore, Date now, final int leewaySeconds) {
        super(String.format("Not-before time is %s and time is now %s (%d leeway seconds is allowed)", notBefore, now, leewaySeconds));
    }
}
