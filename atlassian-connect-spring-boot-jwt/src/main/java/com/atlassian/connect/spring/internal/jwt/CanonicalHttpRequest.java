package com.atlassian.connect.spring.internal.jwt;

import java.util.Map;

/**
 * HTTP request that can be signed for use as a JWT claim.
 */
public interface CanonicalHttpRequest {

    /**
     * HTTP method (e.g. "GET", "POST" etc).
     *
     * @return the HTTP method in upper-case.
     */
    String getMethod();

    /**
     * The part of an absolute URL that is after the protocol, server, port and context (i.e. base) path.
     * E.g. "/the_path" in "http://server:80/context/the_path?param=value" where "/context" is the context path.
     *
     * @return the relative path with no case manipulation.
     */
    String getRelativePath();

    /**
     * The {@link Map} of parameter-name to parameter-values.
     *
     * @return {@link Map} representing { parameter-name-1 to { parameter-value-1, parameter-value-2 ... }, parameter-name-2 to { ... }, ... }
     */
    Map<String, String[]> getParameterMap();
}
