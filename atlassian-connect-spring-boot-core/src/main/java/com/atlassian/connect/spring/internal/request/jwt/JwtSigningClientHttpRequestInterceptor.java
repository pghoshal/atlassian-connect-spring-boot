package com.atlassian.connect.spring.internal.request.jwt;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.AtlassianHostUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

/**
 * A {@link ClientHttpRequestInterceptor} that signs requests to Atlassian hosts with JSON Web Tokens.
 */
@Component
public class JwtSigningClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final String USER_AGENT_PRODUCT = "atlassian-connect-spring-boot";

    @Autowired
    private JwtGenerator jwtGenerator;

    @Autowired
    private AtlassianHostRepository hostRepository;

    @Value("${atlassian.connect.client-version}")
    private String atlassianConnectClientVersion;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpRequest interceptedRequest = getHostUserForRequest(request).map((hostUser) -> wrapRequest(request, hostUser)).orElse(request);
        return execution.execute(interceptedRequest, body);
    }

    protected Optional<AtlassianHostUser> getHostUserForRequest(HttpRequest request) {
        Optional<AtlassianHostUser> optionalHostUser = getHostUserFromSecurityContext()
                .filter((hostUser) -> isRequestToAuthenticatedHost(request, hostUser));
        if (!optionalHostUser.isPresent()) {
            optionalHostUser = getHostUserFromRequestUrl(request.getURI());
        }
        return optionalHostUser;
    }

    private Optional<AtlassianHostUser> getHostUserFromSecurityContext() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(AtlassianHostUser.class::isInstance)
                .map(AtlassianHostUser.class::cast);
    }

    protected boolean isRequestToAuthenticatedHost(HttpRequest request, AtlassianHostUser hostUser) {
        URI hostBaseUri = URI.create(hostUser.getHost().getBaseUrl());
        URI requestUri = request.getURI();
        return !hostBaseUri.relativize(requestUri).isAbsolute();
    }

    private Optional<AtlassianHostUser> getHostUserFromRequestUrl(URI uri) {
        Optional<AtlassianHostUser> optionalHostUser = Optional.empty();
        if (uri.isAbsolute()) {
            optionalHostUser = getHostUserFromBaseUrl(getBaseUrl(uri));
            if (!optionalHostUser.isPresent()) {
                optionalHostUser = getHostUserFromBaseUrl(getBaseUrlWithFirstPathElement(uri));
            }
        }
        return optionalHostUser;
    }

    private Optional<AtlassianHostUser> getHostUserFromBaseUrl(String baseUrl) {
        Optional<AtlassianHost> optionalHost = hostRepository.findFirstByBaseUrl(baseUrl);
        return optionalHost.map((host) -> new AtlassianHostUser(host, Optional.empty()));
    }

    protected String getBaseUrl(URI uri) {
        try {
            return new URI(uri.getScheme(), uri.getAuthority(), null, null, null).toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    protected String getBaseUrlWithFirstPathElement(URI uri) {
        try {
            return new URI(uri.getScheme(), uri.getAuthority(), getFirstPathElement(uri), null, null).toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private String getFirstPathElement(URI uri) {
        String path = uri.getPath();
        if (path != null ) {
            int secondSlashIndex = path.indexOf('/', 1);
            if (secondSlashIndex != -1) {
                path = path.substring(0, secondSlashIndex);
            }
        }
        return path;
    }

    private HttpRequest wrapRequest(HttpRequest request, AtlassianHostUser hostUser) {
        String jwt = createJwt(request.getMethod(), request.getURI(), hostUser);
        URI uri = wrapUri(request, hostUser);
        return new JwtSignedHttpRequestWrapper(request, jwt, uri);
    }

    protected String createJwt(HttpMethod method, URI uri, AtlassianHostUser hostUser) {
        return jwtGenerator.createJwtToken(uri, method, hostUser);
    }

    private URI wrapUri(HttpRequest request, AtlassianHostUser hostUser) {
        URI uri = request.getURI();
        if (!uri.isAbsolute()) {
            URI baseUri = URI.create(hostUser.getHost().getBaseUrl());
            uri = baseUri.resolve(getUriToResolve(baseUri, uri));
        }
        return uri;
    }

    private URI getUriToResolve(URI baseUri, URI uri) {
        String pathToResolve = "";
        String baseUriPath = baseUri.getPath();
        if (baseUriPath != null) {
            pathToResolve += baseUriPath;
        }
        String path = uri.getPath();
        if (path != null) {
            String pathToAppend = (pathToResolve.endsWith("/") && path.startsWith("/")) ? path.substring(1) : path;
            pathToResolve += pathToAppend;
        }

        try {
            uri = new URI(null, null, pathToResolve, uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
        return uri;
    }

    private class JwtSignedHttpRequestWrapper extends HttpRequestWrapper {

        private final String jwt;
        private final URI uri;

        public JwtSignedHttpRequestWrapper(HttpRequest request, String jwt, URI uri) {
            super(request);
            this.jwt = jwt;
            this.uri = uri;

            setJwtHeaders();
        }

        @Override
        public URI getURI() {
            return uri;
        }

        private void setJwtHeaders() {
            HttpHeaders headers = super.getHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, String.format("JWT %s", jwt));
            headers.add(HttpHeaders.USER_AGENT, String.format("%s/%s", USER_AGENT_PRODUCT, atlassianConnectClientVersion));
        }
    }
}
