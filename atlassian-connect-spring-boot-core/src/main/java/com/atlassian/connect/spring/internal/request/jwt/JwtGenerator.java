package com.atlassian.connect.spring.internal.request.jwt;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.descriptor.AddonDescriptorLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * A generator of JSON Web Tokens.
 */
@Component
public class JwtGenerator {

    @Autowired
    private AddonDescriptorLoader addonDescriptorLoader;

    public String createJwtToken(URI uri, HttpMethod httpMethod, AtlassianHostUser hostUser) {
        JwtBuilder jwtBuilder = new JwtBuilder()
                .issuer(addonDescriptorLoader.getDescriptor().getKey())
                // .audience(host.getClientKey()) -- TODO Figure out whether we can / should set this?
                .queryHash(httpMethod, uri, hostUser.getHost().getBaseUrl())
                .signature(hostUser.getHost().getSharedSecret());
        maybeIncludeJwtSubjectClaim(jwtBuilder, hostUser);
        return jwtBuilder.build();
    }

    private JwtBuilder maybeIncludeJwtSubjectClaim(JwtBuilder jwtBuilder, AtlassianHostUser hostUser) {
        if (includeJwtSubjectClaim() && hostUser.getUserKey().isPresent()) {
            jwtBuilder.subject(hostUser.getUserKey().get());
        }
        return jwtBuilder;
    }

    private boolean includeJwtSubjectClaim() {
        // TODO Check a property
        return false;
    }
}
