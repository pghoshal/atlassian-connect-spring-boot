package com.atlassian.connect.spring.test.request.jwt;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.test.util.AtlassianHosts;
import com.atlassian.connect.spring.test.util.BaseApplicationTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.connect.spring.test.util.AtlassianHosts.createAndSaveHost;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class JwtSigningTest extends BaseApplicationTest {

    @Autowired
    private AtlassianHostRepository hostRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${atlassian.connect.client-version}")
    private String atlassianConnectClientVersion;

    private MockRestServiceServer mockServer;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void shouldNotSignRequestToUnknownHostWithoutAuthenticatedHost() {
        expectRequestWithoutJwt(getAbsoluteRequestUrl());
    }

    @Test
    public void shouldSignRequestToStoredHostWithoutAuthenticatedHost() {
        createAndSaveHost(hostRepository);
        String url = getAbsoluteRequestUrl();
        expectRequestWithJwt(url, url);
    }

    @Test
    public void shouldSignAbsoluteRequestToAuthenticatedHost() {
        AtlassianHost host = createAndSaveHost(hostRepository);
        setJwtAuthenticatedPrincipal(host);
        String url = getAbsoluteRequestUrl();
        expectRequestWithJwt(url, url);
    }

    @Test
    public void shouldSignRelativeRequestToAuthenticatedHost() {
        AtlassianHost host = createAndSaveHost(hostRepository);
        setJwtAuthenticatedPrincipal(host);
        String relativeUrl = getRelativeRequestUrl();
        expectRequestWithJwt(relativeUrl, AtlassianHosts.BASE_URL + relativeUrl);
    }

    @Test
    public void shouldNotSignAbsoluteRequestToOtherThanAuthenticatedHost() {
        AtlassianHost host = createAndSaveHost(hostRepository);
        setJwtAuthenticatedPrincipal(host);
        expectRequestWithoutJwt("http://other-host.com");
    }

    @Test
    public void shouldNotAddMultipleJwtHeadersToRequest() {
        AtlassianHost host = createAndSaveHost(hostRepository);
        setJwtAuthenticatedPrincipal(host);
        String url = getAbsoluteRequestUrl();

        withACustomInterceptor(() -> {
            mockServer.expect(requestTo(url))
                    .andExpect(authorizationHeaderWithJwt())
                    .andExpect(onlyOneAuthorizationHeader())
                    .andRespond(withSuccess());
            restTemplate.getForObject(url, Void.class);
            mockServer.verify();
        });
    }

    private void expectRequestWithoutJwt(String requestUrl) {
        mockServer.expect(requestTo(requestUrl))
                .andExpect(authorizationHeaderWithJwt())
                .andRespond(withSuccess());
        try {
            restTemplate.getForObject(requestUrl, Void.class);
        } catch (AssertionError e) {
            assertThat(e.getMessage(), is("Expected header <Authorization>"));
        }
        mockServer.verify();
    }

    private void expectRequestWithJwt(String requestUrl, String expectedRequestUrl) {
        mockServer.expect(requestTo(expectedRequestUrl))
                .andExpect(authorizationHeaderWithJwt())
                .andExpect(userAgentHeader())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess());
        restTemplate.getForObject(requestUrl, Void.class);
        mockServer.verify();
    }

    private static String getAbsoluteRequestUrl() {
        return AtlassianHosts.BASE_URL + getRelativeRequestUrl();
    }

    private static String getRelativeRequestUrl() {
        return "/api";
    }

    @SuppressWarnings("unchecked")
    public static RequestMatcher authorizationHeaderWithJwt() {
        return header(HttpHeaders.AUTHORIZATION, startsWith("JWT "));
    }

    private RequestMatcher userAgentHeader() {
        assertThat(atlassianConnectClientVersion, is(notNullValue()));
        return header(HttpHeaders.USER_AGENT, is("atlassian-connect-spring-boot/" + atlassianConnectClientVersion));
    }

    private static RequestMatcher onlyOneAuthorizationHeader() {
        return request -> {
            List<String> values = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
            assertThat("Authorization header", values, hasSize(1));
        };
    }

    @SuppressWarnings("unused")
    private void withACustomInterceptor(Runnable runnable) {
        List<ClientHttpRequestInterceptor> originalInterceptors = restTemplate.getInterceptors();
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>(restTemplate.getInterceptors());

        ClientHttpRequestInterceptor innocentInterceptor = (request, body, execution) -> {
            HttpHeaders unused = request.getHeaders();
            return execution.execute(request, body);
        };
        interceptors.add(innocentInterceptor);

        restTemplate.setInterceptors(interceptors);

        try {
            runnable.run();
        } finally {
            restTemplate.setInterceptors(originalInterceptors);
        }
    }
}
