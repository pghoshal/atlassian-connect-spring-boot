package com.atlassian.connect.spring.internal.jira.rest;

import com.atlassian.fugue.Option;
import com.atlassian.httpclient.apache.httpcomponents.DefaultResponse;
import com.atlassian.httpclient.apache.httpcomponents.Headers;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.httpclient.api.ResponsePromises;
import com.atlassian.httpclient.base.AbstractHttpClient;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Pattern;

public class RestTemplateHttpClient extends AbstractHttpClient implements DisposableHttpClient {

    private RestTemplate restTemplate;

    public RestTemplateHttpClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public ResponsePromise execute(Request request) {
        Response response = restTemplate.execute(request.getUri(), toHttpMethod(request.getMethod()),
                new RestTemplateRequestCallback(request), new RestTemplateResponseExtractor());
        Promise<Response> responsePromise = Promises.forFuture(new AsyncResult<>(response));
        return ResponsePromises.toResponsePromise(responsePromise);
    }

    @Override
    public void flushCacheByUriPattern(Pattern uriPattern) {
        throw new UnsupportedOperationException();
    }

    private HttpMethod toHttpMethod(Request.Method method) {
        switch (method) {
            case GET:
                return HttpMethod.GET;
            case POST:
                return HttpMethod.POST;
            case PUT:
                return HttpMethod.PUT;
            case DELETE:
                return HttpMethod.DELETE;
            case OPTIONS:
                return HttpMethod.OPTIONS;
            case HEAD:
                return HttpMethod.HEAD;
            case TRACE:
                return HttpMethod.TRACE;
            default:
                throw new IllegalStateException("Unmapped HTTP request method");
        }
    }

    @Override
    public void destroy() throws Exception {}

    private static class RestTemplateRequestCallback implements RequestCallback {

        private final Request request;

        public RestTemplateRequestCallback(Request request) {
            this.request = request;
        }

        @Override
        public void doWithRequest(ClientHttpRequest httpRequest) throws IOException {
            httpRequest.getHeaders().setAll(request.getHeaders());

            if (request.hasEntity()) {
                StreamUtils.copy(request.getEntityStream(), httpRequest.getBody());
            }
        }
    }

    private static class RestTemplateResponseExtractor implements ResponseExtractor<Response> {

        @Override
        public Response extractData(ClientHttpResponse clientResponse) throws IOException {
            Map<String, String> headerMap = clientResponse.getHeaders().toSingleValueMap();
            Headers headers = new Headers.Builder().setHeaders(headerMap).build();
            InputStream entityStream = copyInputStream(clientResponse.getBody());
            Option<Long> maxEntitySize = Option.none();
            int statusCode = clientResponse.getStatusCode().value();
            String statusText = clientResponse.getStatusText();
            return new DefaultResponse(headers, entityStream, maxEntitySize, statusCode, statusText);
        }

        private InputStream copyInputStream(InputStream input) throws IOException {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            StreamUtils.copy(input, output);
            return new ByteArrayInputStream(output.toByteArray());
        }
    }
}
