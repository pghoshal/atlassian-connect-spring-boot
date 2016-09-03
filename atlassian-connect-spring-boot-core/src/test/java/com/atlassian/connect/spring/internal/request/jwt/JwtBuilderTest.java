package com.atlassian.connect.spring.internal.request.jwt;

import com.atlassian.connect.spring.internal.jwt.CanonicalHttpRequest;
import org.junit.Test;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

public class JwtBuilderTest {

    private JwtBuilder jwtBuilder = new JwtBuilder();

    @Test
    public void shouldCreateCanonicalHttpRequestForForUrlWithQueryParameters() throws URISyntaxException {
        HttpMethod httpMethod = HttpMethod.POST;
        URI uri = URI.create("http://example.com/api/12345?parameter=some.long_value");
        String host = new URI(uri.getScheme(), uri.getAuthority(), null, null, null).toString();
        CanonicalHttpRequest canonicalHttpRequest = jwtBuilder.createCanonicalHttpRequest(httpMethod, uri, host);
        Map<?, ?> parameterMap = canonicalHttpRequest.getParameterMap();
        assertThat(parameterMap, hasEntry("parameter", new String[] {"some.long_value"}));
        assertThat(parameterMap.get("parameter").getClass().getComponentType(), equalTo(String.class));
    }
}
