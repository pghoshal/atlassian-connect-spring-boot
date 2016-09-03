package com.atlassian.connect.spring.test.auth;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.test.util.BaseApplicationTest;
import com.atlassian.connect.spring.test.util.SimpleJwtSigningRestTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Optional;

import static com.atlassian.connect.spring.test.util.AtlassianHosts.createAndSaveHost;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebIntegrationTest
public class ManagementAuthenticationTest extends BaseApplicationTest {

    private static final String ENDPOINT_PATH = "/beans";

    @Value("${management.context-path}")
    private String managementContextPath;

    @Value("${security.user.name}")
    private String adminUserName;

    @Value("${security.user.password}")
    private String adminUserPassword;

    @Autowired
    private AtlassianHostRepository hostRepository;

    @Test
    public void shouldRejectRequestToManagementEndpointWithoutBasicAuth() throws Exception {
        mvc.perform(get(getManagementEndpointPath()))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Basic ")));
    }

    @Test
    public void shouldRejectRequestToManagementEndpointWithIncorrectCredentials() throws Exception {
        mvc.perform(get(getManagementEndpointPath()).with(httpBasic(adminUserName, "incorrect-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Basic ")));
    }

    @Test
    public void shouldAllowRequestToManagementEndpointWithCorrectCredentials() throws Exception {
        mvc.perform(get(getManagementEndpointPath()).with(httpBasic(adminUserName, adminUserPassword)))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldNotAllowUnauthenticatedRequestToManagementEndpointAfterAuthenticatedFirstRequest() throws Exception {
        mvc.perform(get(getManagementEndpointPath()).with(httpBasic(adminUserName, adminUserPassword)))
                .andExpect(status().isOk());

        mvc.perform(get(getManagementEndpointPath()))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Basic ")));
    }

    @Test
    public void shouldRejectRequestToManagementEndpointWithJwtAuth() {
        AtlassianHost host = createAndSaveHost(hostRepository);
        RestTemplate restTemplate = new SimpleJwtSigningRestTemplate(host, Optional.empty());
        ResponseEntity<Void> response = restTemplate.getForEntity(URI.create(getServerAddress() + getManagementEndpointPath()), Void.class);
        assertThat(response.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
    }

    private String getManagementEndpointPath() {
        return managementContextPath + ENDPOINT_PATH;
    }
}
