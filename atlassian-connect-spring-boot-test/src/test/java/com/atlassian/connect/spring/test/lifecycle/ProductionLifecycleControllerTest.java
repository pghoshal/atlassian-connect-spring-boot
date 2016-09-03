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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Optional;

import static com.atlassian.connect.spring.test.lifecycle.LifecycleBodyHelper.createLifecycleEventMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("production")
// Setting a profile causes Spring to start up a separate Tomcat container - we need to set a different port to avoid clashes
@WebIntegrationTest(randomPort = true)
public class ProductionLifecycleControllerTest extends BaseApplicationTest {

    @Value("${local.server.port}")
    String port;

    @Value("${hello-world.installed-url}")
    private String installedUrl;

    @Test
    public void shouldRejectInstallFromUnknownHostInProdMode() throws Exception {
        AtlassianHost host = AtlassianHosts.createHost();
        RestTemplate restTemplate = new SimpleJwtSigningRestTemplate(host, Optional.empty());
        ResponseEntity<String> response = restTemplate.postForEntity(URI.create("http://localhost:" + port + installedUrl),
                createLifecycleEventMap("installed"), String.class);
        assertThat(response.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
    }
}
