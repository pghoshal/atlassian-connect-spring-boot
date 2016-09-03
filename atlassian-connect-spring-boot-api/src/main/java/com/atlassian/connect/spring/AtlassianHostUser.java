package com.atlassian.connect.spring;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;

/**
 * The authentication principal for requests coming from an Atlassian host application in which the
 * add-on is installed.
 *
 * This class is the Atlassian Connect equivalent of Spring Security's {@link User} and can be resolved as a
 * controller argument by using the {@link AuthenticationPrincipal} annotation.
 */
public final class AtlassianHostUser {

    private AtlassianHost host;

    private Optional<String> userKey;

    protected AtlassianHostUser() {}

    /**
     * Creates a new Atlassian host principal.
     *
     * @param host the host from which the request originated
     * @param optionalUserKey the key of the user on whose behalf a request was made (optional)
     */
    public AtlassianHostUser(AtlassianHost host, Optional<String> optionalUserKey) {
        this.host = host;
        this.userKey = optionalUserKey;
    }

    /**
     * Returns the host from which the request originated.
     *
     * @return the Atlassian host
     */
    public AtlassianHost getHost() {
        return host;
    }

    /**
     * The the key of the user on whose behalf a request was made.
     *
     * @return the user key
     */
    public Optional<String> getUserKey() {
        return userKey;
    }
}
