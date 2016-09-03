package com.atlassian.connect.spring.internal.request.jwt;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.http.HttpRequest;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.runners.Parameterized.Parameters;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class JwtSigningClientHttpRequestInterceptorTest {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"http://example.com/", "http://example.com/", true},
                {"http://example.com/", "http://example.com/api", true},
                {"http://example.com/", "http://example.com/?some=value", true},
                {"http://example.com/", "https://example.com/", false},
                {"http://example.com/", "/api", true},
                {"http://other-host.com/", "https://example.com/", false}
        });
    }

    private final String baseUrl;
    private final String requestUrl;
    private final boolean expected;

    private JwtSigningClientHttpRequestInterceptor interceptor = new JwtSigningClientHttpRequestInterceptor();

    public JwtSigningClientHttpRequestInterceptorTest(String baseUrl, String requestUrl, boolean expected) {
        this.baseUrl = baseUrl;
        this.requestUrl = requestUrl;
        this.expected = expected;
    }

    @Test
    public void shouldAcceptBaseUrlAsRequestToAuthenticatedHost() {
        assertThat(isRequestToAuthenticatedHost(baseUrl, requestUrl), is(expected));
    }

    private boolean isRequestToAuthenticatedHost(String baseUrl, String requestUrl) {
        return interceptor.isRequestToAuthenticatedHost(mockRequest(requestUrl), createHostUser(baseUrl));
    }

    private HttpRequest mockRequest(String requestUrl) {
        HttpRequest httpRequest = mock(HttpRequest.class);
        when(httpRequest.getURI()).thenReturn(URI.create(requestUrl));
        return httpRequest;
    }

    private AtlassianHostUser createHostUser(String baseUrl) {
        AtlassianHost host = new AtlassianHost();
        host.setBaseUrl(baseUrl);
        return new AtlassianHostUser(host, Optional.<String>empty());
    }
}
