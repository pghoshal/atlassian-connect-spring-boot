package com.atlassian.connect.spring.internal.request.jwt;

import com.atlassian.connect.spring.internal.jwt.CanonicalHttpRequest;
import com.atlassian.connect.spring.internal.jwt.CanonicalHttpUriRequest;
import com.atlassian.connect.spring.internal.jwt.HttpRequestCanonicalizer;
import com.atlassian.connect.spring.internal.jwt.JwtJsonBuilder;
import com.atlassian.connect.spring.internal.jwt.JwtWriter;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.MACSigner;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A builder of JSON Web Tokens.
 */
public class JwtBuilder {

    private JwtJsonBuilder jwtJsonBuilder;

    private String sharedSecret;

    private Supplier<String> queryHashSupplier;

    public JwtBuilder() {
        this.jwtJsonBuilder = new JwtJsonBuilder();
    }

    public JwtBuilder issuer(String iss) {
        jwtJsonBuilder.issuer(iss);
        return this;
    }

    public JwtBuilder subject(String sub) {
        jwtJsonBuilder.subject(sub);
        return this;
    }

    public JwtBuilder audience(String aud) {
        jwtJsonBuilder.audience(aud);
        return this;
    }

    public JwtBuilder expirationTime(long exp) {
        jwtJsonBuilder.expirationTime(exp);
        return this;
    }

    public JwtBuilder notBefore(long nbf) {
        jwtJsonBuilder.notBefore(nbf);
        return this;
    }

    public JwtBuilder issuedAt(long iat) {
        jwtJsonBuilder.issuedAt(iat);
        return this;
    }

    public JwtBuilder queryHash(HttpMethod httpMethod, URI uri, String baseUrl) {
        queryHashSupplier = () -> computeCanonicalRequestHash(httpMethod, uri, baseUrl);
        return this;
    }

    public JwtBuilder claim(String name, Object value) {
        jwtJsonBuilder.claim(name, value);
        return this;
    }

    public JwtBuilder signature(String sharedSecret) {
        this.sharedSecret = sharedSecret;
        return this;
    }

    public String build() {
        jwtJsonBuilder.queryHash(queryHashSupplier.get());
        String jwtPayload = jwtJsonBuilder.build();
        return createJwtWriter().jsonToJwt(jwtPayload);
    }

    private JwtWriter createJwtWriter() {
        return new JwtWriter(JWSAlgorithm.HS256, new MACSigner(sharedSecret));
    }

    public String toString() {
        return build();
    }

    protected String computeCanonicalRequestHash(HttpMethod httpMethod, URI uri, String baseUrl) {
        CanonicalHttpRequest canonicalHttpRequest = createCanonicalHttpRequest(httpMethod, uri, baseUrl);
        try {
            return HttpRequestCanonicalizer.computeCanonicalRequestHash(canonicalHttpRequest);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    protected CanonicalHttpRequest createCanonicalHttpRequest(HttpMethod httpMethod, URI uri, String baseUrl) {
        final MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(uri).build().getQueryParams();
        final URI hostBaseUri = UriComponentsBuilder.fromHttpUrl(baseUrl).build().toUri();
        return new CanonicalHttpUriRequest(httpMethod.name(), uri.getPath(), hostBaseUri.getPath(), toArrayMap(queryParams));
    }

    private static Map<String, String[]> toArrayMap(MultiValueMap<String, String> queryParams) {
        final Map<String, String[]> result = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
        }

        return result;
    }
}
