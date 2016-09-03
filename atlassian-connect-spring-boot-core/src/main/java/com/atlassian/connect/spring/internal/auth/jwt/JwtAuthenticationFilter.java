package com.atlassian.connect.spring.internal.auth.jwt;

import com.atlassian.connect.spring.internal.AtlassianConnectProperties;
import com.atlassian.connect.spring.internal.descriptor.AddonDescriptor;
import com.atlassian.connect.spring.internal.descriptor.AddonDescriptorLoader;
import com.atlassian.connect.spring.internal.jwt.CanonicalHttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

/**
 * A servlet filter that extracts JSON Web Tokens from the Authorization request header and from the <code>jwt</code>
 * query parameter for use as authentication tokens.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER_SCHEME_PREFIX = "JWT ";

    private static final String QUERY_PARAMETER_NAME = "jwt";

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private AuthenticationManager authenticationManager;

    private AuthenticationFailureHandler failureHandler;

    @Autowired
    private AddonDescriptorLoader addonDescriptorLoader;

    @Autowired
    private AtlassianConnectProperties atlassianConnectProperties;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, ServerProperties serverProperties) {
        this.authenticationManager = authenticationManager;
        this.failureHandler = createFailureHandler(serverProperties);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional<String> optionalJwt = getJwtFromRequest(request);
        if (optionalJwt.isPresent()) {
            Authentication authenticationRequest = createJwtAuthenticationToken(request, getJwtFromRequest(request).get());

            Authentication authenticationResult;
            try {
                authenticationResult = authenticationManager.authenticate(authenticationRequest);
                SecurityContextHolder.getContext().setAuthentication(authenticationResult);
            } catch (AuthenticationException e) {
                if (!shouldIgnoreInvalidJwt(request, e)) {
                    failureHandler.onAuthenticationFailure(request, response, e);
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private SimpleUrlAuthenticationFailureHandler createFailureHandler(ServerProperties serverProperties) {
        SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler(serverProperties.getError().getPath());
        failureHandler.setAllowSessionCreation(false);
        failureHandler.setUseForward(true);
        return failureHandler;
    }

    private static Optional<String> getJwtFromRequest(HttpServletRequest request) {
        Optional<String> optionalJwt = getJwtFromHeader(request);
        if (!optionalJwt.isPresent()) {
            optionalJwt = getJwtFromParameter(request);
        }
        return optionalJwt;
    }

    private static Optional<String> getJwtFromHeader(HttpServletRequest request) {
        Optional<String> optionalJwt = Optional.empty();
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.isEmpty(authHeader) && authHeader.startsWith(AUTHORIZATION_HEADER_SCHEME_PREFIX)) {
            String jwt = authHeader.substring(AUTHORIZATION_HEADER_SCHEME_PREFIX.length());
            optionalJwt = Optional.of(jwt);
        }

        return optionalJwt;
    }

    private static Optional<String> getJwtFromParameter(HttpServletRequest request) {
        Optional<String> optionalJwt = Optional.empty();
        String jwt = request.getParameter(QUERY_PARAMETER_NAME);
        if (!StringUtils.isEmpty(jwt)) {
            optionalJwt = Optional.of(jwt);
        }
        return optionalJwt;
    }

    private JwtAuthenticationToken createJwtAuthenticationToken(HttpServletRequest request, String jwt) {
        log.debug("Retrieved JWT from request {}", jwt);
        CanonicalHttpServletRequest canonicalHttpServletRequest = new CanonicalHttpServletRequest(request);
        JwtCredentials credentials = new JwtCredentials(jwt, canonicalHttpServletRequest);
        return new JwtAuthenticationToken(credentials);
    }

    private boolean shouldIgnoreInvalidJwt(HttpServletRequest request, AuthenticationException e) {
        return e instanceof UsernameNotFoundException
                && ((isRequestToInstalledLifecycle(request) && atlassianConnectProperties.isAllowReinstallMissingHost())
                || isRequestToUninstalledLifecycle(request));
    }

    private boolean isRequestToInstalledLifecycle(HttpServletRequest request) {
        AddonDescriptor descriptor = addonDescriptorLoader.getDescriptor();
        String url = descriptor.getBaseUrl() + descriptor.getInstalledLifecycleUrl();
        return isRequestToUrl(request, url);
    }

    private boolean isRequestToUninstalledLifecycle(HttpServletRequest request) {
        AddonDescriptor descriptor = addonDescriptorLoader.getDescriptor();
        String url = descriptor.getBaseUrl() + descriptor.getUninstalledLifecycleUrl();
        return isRequestToUrl(request, url);
    }

    private boolean isRequestToUrl(HttpServletRequest request, String url) {
        UriComponents urlComponents = UriComponentsBuilder.fromUri(URI.create(url)).build();
        UriComponents requestComponents = UriComponentsBuilder.fromUri(URI.create(request.getRequestURL().toString()))
                .query(request.getQueryString()).build();
        return StringUtils.equals(requestComponents.getPath(), urlComponents.getPath())
                && requestComponents.getQueryParams().entrySet().containsAll(urlComponents.getQueryParams().entrySet());
    }
}
