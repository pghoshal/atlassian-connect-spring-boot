package com.atlassian.connect.spring.it.jira.rest;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.internal.jira.rest.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class JiraIssueRestClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JiraRestClientFactory jiraRestClientFactory;

    public Issue getIssue(AtlassianHost host, String issueKey) {
        JiraRestClient jiraRestClient = jiraRestClientFactory.createJiraRestClient(host, restTemplate);
        return jiraRestClient.getIssueClient().getIssue(issueKey).claim();
    }
}
