package com.atlassian.connect.spring.test.lifecycle;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.test.util.AtlassianHosts;
import com.atlassian.connect.spring.test.util.BaseApplicationTest;
import com.atlassian.connect.spring.test.util.SimpleJwtSigningRestTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Optional;

import static com.atlassian.connect.spring.test.lifecycle.LifecycleBodyHelper.createLifecycleEventMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@WebIntegrationTest
public class DevelopmentLifecycleControllerTest extends BaseApplicationTest {

    @Value("${hello-world.installed-url}")
    private String installedUrl;

    @Test
    public void shouldAcceptInstallFromUnknownHostInDevMode() throws Exception {
        AtlassianHost host = AtlassianHosts.createHost();
        RestTemplate restTemplate = new SimpleJwtSigningRestTemplate(host, Optional.empty());
        ResponseEntity<String> response = restTemplate.postForEntity(URI.create(getServerAddress() + installedUrl),
                createLifecycleEventMap("installed"), String.class);
        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT));
    }
}
