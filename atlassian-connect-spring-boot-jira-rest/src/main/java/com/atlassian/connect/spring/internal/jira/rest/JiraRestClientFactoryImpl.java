package com.atlassian.connect.spring.internal.jira.rest;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClient;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.net.URI;

@Component
public class JiraRestClientFactoryImpl implements JiraRestClientFactory {

    @Override
    public JiraRestClient createJiraRestClient(AtlassianHost host, RestTemplate restTemplate) {
        URI serverUri = URI.create(host.getBaseUrl());
        DisposableHttpClient httpClient = new RestTemplateHttpClient(restTemplate);
        return new AsynchronousJiraRestClient(serverUri, httpClient);
    }
}
