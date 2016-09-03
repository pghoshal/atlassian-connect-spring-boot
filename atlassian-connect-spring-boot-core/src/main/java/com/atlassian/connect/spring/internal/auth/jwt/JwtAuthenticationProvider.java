package com.atlassian.connect.spring.internal.auth.jwt;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.jwt.HttpRequestCanonicalizer;
import com.atlassian.connect.spring.internal.jwt.Jwt;
import com.atlassian.connect.spring.internal.jwt.JwtExpiredException;
import com.atlassian.connect.spring.internal.jwt.JwtParseException;
import com.atlassian.connect.spring.internal.jwt.JwtParser;
import com.atlassian.connect.spring.internal.jwt.JwtReader;
import com.atlassian.connect.spring.internal.jwt.JwtVerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * An {@link AuthenticationProvider} for JSON Web Tokens. In addition to verifying the signature of the JWT, the query
 * string hash claim specific to Atlassian Connect is also verified.
 */
@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationProvider.class);

    private static final Class<JwtAuthenticationToken> TOKEN_CLASS = JwtAuthenticationToken.class;

    @Autowired
    private AtlassianHostRepository hostRepository;

    @Override
    public boolean supports(Class<?> authenticationClass) {
        return authenticationClass.equals(TOKEN_CLASS);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JwtCredentials jwtCredentials = getJwtCredentials(authentication);
        AtlassianHost host = getHost(jwtCredentials);
        Jwt verifiedToken = verifyToken(jwtCredentials, host);
        log.info("Authenticated token for host: " + host.getClientKey());
        return new JwtAuthentication(new AtlassianHostUser(host, Optional.ofNullable(verifiedToken.getSubject())), verifiedToken);
    }

    private JwtCredentials getJwtCredentials(Authentication authentication) {
        JwtAuthenticationToken authenticationToken = TOKEN_CLASS.cast(authentication);
        return authenticationToken.getCredentials();
    }

    private Jwt parseToken(String jwt) throws AuthenticationException {
        try {
            return new JwtParser().parse(jwt);
        } catch (JwtParseException e) {
            log.error(e.getMessage());
            throw new InvalidJwtException(e.getMessage(), e);
        }
    }

    private AtlassianHost getHost(JwtCredentials jwtCredentials) throws AuthenticationException {
        final Jwt unverifiedToken = parseToken(jwtCredentials.getRawJwt());
        String issuerClientKey = unverifiedToken.getIssuer();
        Optional<AtlassianHost> potentialHost = Optional.ofNullable(hostRepository.findOne(issuerClientKey));
        if (!potentialHost.isPresent()) {
            String errorMsg = "Could not find an installed host for the provided issuer: " + issuerClientKey;
            log.debug(errorMsg);
            throw new UsernameNotFoundException(errorMsg);
        }
        return potentialHost.get();
    }

    private Jwt verifyToken(JwtCredentials jwtCredentials, AtlassianHost host) throws AuthenticationException {
        String queryStringHash = computeQueryStringHash(jwtCredentials);
        try {
            return new JwtReader(host.getSharedSecret()).readAndVerify(jwtCredentials.getRawJwt(), queryStringHash);
        } catch (JwtParseException e) {
            log.error(e.getMessage());
            throw new InvalidJwtException(e.getMessage(), e);
        } catch (JwtExpiredException e) {
            log.error(e.getMessage());
            throw new CredentialsExpiredException(e.getMessage());
        } catch (JwtVerificationException e) {
            log.error(e.getMessage());
            throw new BadCredentialsException(e.getMessage(), e);
        }
    }

    private String computeQueryStringHash(JwtCredentials jwtCredentials) {
        try {
            return HttpRequestCanonicalizer.computeCanonicalRequestHash(jwtCredentials.getCanonicalHttpRequest());
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }
}
