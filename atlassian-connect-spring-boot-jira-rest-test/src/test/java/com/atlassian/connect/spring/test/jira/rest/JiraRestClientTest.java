package com.atlassian.connect.spring.test.jira.rest;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.it.jira.rest.AtlassianConnectJiraRestTestApplication;
import com.atlassian.connect.spring.it.jira.rest.JiraIssueRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;

import static com.atlassian.connect.spring.test.request.jwt.JwtSigningTest.authorizationHeaderWithJwt;
import static com.atlassian.connect.spring.test.util.AtlassianHosts.createAndSaveHost;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(AtlassianConnectJiraRestTestApplication.class)
public class JiraRestClientTest {

    public static final String ISSUE_KEY = "TEST-1";

    @Autowired
    private AtlassianHostRepository hostRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JiraIssueRestClient jiraIssueRestClient;

    private MockRestServiceServer mockServer;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void shouldSignRequestToStoredHostWithoutAuthenticatedHost() throws IOException {
        AtlassianHost host = createAndSaveHost(hostRepository);
        mockServer.expect(requestTo(getIssueApiUrl(host)))
                .andExpect(authorizationHeaderWithJwt())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(getIssueJson(), MediaType.APPLICATION_JSON));
        Issue issue = jiraIssueRestClient.getIssue(host, ISSUE_KEY);
        mockServer.verify();

        assertThat(issue.getKey(), equalTo(ISSUE_KEY));
    }

    private String getIssueApiUrl(AtlassianHost host) {
        return String.format("%s/rest/api/latest/issue/%s?expand=schema,names,transitions", host.getBaseUrl(), ISSUE_KEY);
    }

    private String getIssueJson() throws IOException {
        return StreamUtils.copyToString(JiraRestClientTest.class.getResourceAsStream("/issue.json"), Charset.defaultCharset());
    }
}
