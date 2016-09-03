package com.atlassian.connect.spring.internal.auth.jwt;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * An authentication exception thrown when processing a JSON Web Token that could not be successfully parsed.
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Invalid JWT")
public class InvalidJwtException extends AuthenticationException {

    public InvalidJwtException(String message, Throwable cause) {
        super(message, cause);
    }
}
