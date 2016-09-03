package com.atlassian.connect.spring.internal.auth.jwt;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.jwt.Jwt;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/**
 * An authentication object representing a verified and accepted JSON Web Token.
 *
 * @see JwtAuthenticationToken
 */
public class JwtAuthentication implements Authentication {

    public static final String ROLE_JWT = "ROLE_JWT";

    private final AtlassianHostUser hostUser;
    private final Jwt jwt;

    public JwtAuthentication(AtlassianHostUser hostUser, Jwt jwt) {
        this.hostUser = hostUser;
        this.jwt = jwt;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(ROLE_JWT));
    }

    @Override
    public Object getCredentials() {
        return jwt;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return hostUser;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean b) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(hostUser.getHost().getClientKey());
        hostUser.getUserKey().ifPresent((userKey) -> {
            nameBuilder.append(String.format(" (%s)", userKey));
        });
        return nameBuilder.toString();
    }
}
