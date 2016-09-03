package com.atlassian.connect.spring.internal.jira.rest;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import org.springframework.web.client.RestTemplate;

public interface JiraRestClientFactory {

    JiraRestClient createJiraRestClient(AtlassianHost host, RestTemplate restTemplate);
}
