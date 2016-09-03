package com.atlassian.connect.spring.test.auth.jwt;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.request.jwt.JwtBuilder;
import com.atlassian.connect.spring.test.util.BaseApplicationTest;
import com.atlassian.connect.spring.test.util.SimpleJwtSigningClientHttpRequestInterceptor;
import com.atlassian.connect.spring.test.util.SimpleJwtSigningRestTemplate;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

import static com.atlassian.connect.spring.test.util.AtlassianHosts.createAndSaveHost;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@WebIntegrationTest
public class JwtVerificationTest extends BaseApplicationTest {

    @Autowired
    private AtlassianHostRepository hostRepository;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldReturnPrincipalDetailsForValidJwtHeader() throws Exception {
        AtlassianHost host = createAndSaveHost(hostRepository);
        String subject = "charlie";
        RestTemplate restTemplate = new SimpleJwtSigningRestTemplate(host, Optional.of(subject));
        ResponseEntity<AtlassianHostUser> response = restTemplate.getForEntity(getRequestUri(), AtlassianHostUser.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getHost().getClientKey(), is(host.getClientKey()));
        assertThat(response.getBody().getUserKey(), is(Optional.of(subject)));
    }

    @Test
    public void shouldReturnPrincipalDetailsForValidJwtQueryParameter() throws Exception {
        AtlassianHost host = createAndSaveHost(hostRepository);
        SimpleJwtSigningClientHttpRequestInterceptor interceptor = new SimpleJwtSigningClientHttpRequestInterceptor(
                host.getClientKey(), host.getSharedSecret(), Optional.empty());
        UriComponentsBuilder requestUriBuilder = getRequestUriBuilder();
        String jwt = interceptor.createJwt(HttpMethod.GET, requestUriBuilder.build().toUri(), null);
        URI requestUri = requestUriBuilder.queryParam("jwt", jwt).build().toUri();
        ResponseEntity<AtlassianHostUser> response = new RestTemplate().getForEntity(requestUri, AtlassianHostUser.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getHost().getClientKey(), is(host.getClientKey()));
        assertThat(response.getBody().getUserKey(), is(Optional.empty()));
    }

    @Test
    public void shouldRejectRequestWithoutJwt() throws Exception {
        RestTemplate restTemplate = new TestRestTemplate();
        ResponseEntity<Void> response = restTemplate.getForEntity(getRequestUri(), Void.class);
        assertThat(response.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
        assertThat(response.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE), startsWith("JWT "));
    }

    @Test
    public void shouldRejectRequestWithMalformedJwt() throws Exception {
        ResponseEntity<AtlassianHostUser> response = getWithJwt("malformed-jwt");
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void shouldRejectRequestForUnknownHost() throws Exception {
        ResponseEntity<AtlassianHostUser> response = getWithJwt(createJwtBuilder("unknown-host", "some-secret", getRequestUri()).build());
        assertThat(response.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void shouldRejectRequestWithExpiredJwt() throws Exception {
        AtlassianHost host = createAndSaveHost(hostRepository);
        long issuedAt = System.currentTimeMillis() / 1000 + -1001L;
        long expirationTime = issuedAt + 1L;
        ResponseEntity<AtlassianHostUser> response = getWithJwt(
                createJwtBuilder(host.getClientKey(), host.getSharedSecret(), getRequestUri())
                .issuedAt(issuedAt)
                .expirationTime(expirationTime).build());
        assertThat(response.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void shouldRejectRequestWithInvalidSecret() throws Exception {
        AtlassianHost host = createAndSaveHost(hostRepository);
        ResponseEntity<AtlassianHostUser> response = getWithJwt(
                createJwtBuilder(host.getClientKey(), "invalid-secret", getRequestUri()).build());
        assertThat(response.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void shouldRejectRequestWithInvalidQsh() throws Exception {
        AtlassianHost host = createAndSaveHost(hostRepository);
        ResponseEntity<AtlassianHostUser> response = getWithJwt(createJwtBuilder(
                host.getClientKey(), host.getSharedSecret(), URI.create(getServerAddress() + "/not/valid")).build());
        assertThat(response.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void shouldUnauthenticatedAcceptRequestToSecurityIgnoredPath() {
        RestTemplate restTemplate = new TestRestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(URI.create(getServerAddress() + "/no-auth"), String.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is("No authentication required"));
    }

    private ResponseEntity<AtlassianHostUser> getWithJwt(String jwt) {
        return new SimpleJwtSigningRestTemplate(jwt).getForEntity(getRequestUri(), AtlassianHostUser.class);
    }

    private JwtBuilder createJwtBuilder(String clientKey, String sharedSecret, URI requestUri) {
        return new JwtBuilder()
                .issuer(clientKey)
                .queryHash(HttpMethod.GET, requestUri, getServerAddress())
                .signature(sharedSecret);
    }

    private URI getRequestUri() {
        return getRequestUriBuilder().build().toUri();
    }

    private UriComponentsBuilder getRequestUriBuilder() {
        return UriComponentsBuilder.fromUri(URI.create(getServerAddress() + "/jwt"));
    }

    @After
    public void cleanup() {
        hostRepository.deleteAll();
    }
}
