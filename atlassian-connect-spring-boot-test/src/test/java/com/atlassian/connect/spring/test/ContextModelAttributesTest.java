package com.atlassian.connect.spring.test;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.test.util.BaseApplicationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static com.atlassian.connect.spring.test.util.AtlassianHosts.createAndSaveHost;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class ContextModelAttributesTest extends BaseApplicationTest {

    @Autowired
    private AtlassianHostRepository hostRepository;

    @Test
    public void shouldReturnAllJsModelAttributeForAuthenticated() throws Exception {
        AtlassianHost host = createAndSaveHost(hostRepository);
        setJwtAuthenticatedPrincipal(host);
        String expectedUrl = String.format("%s/atlassian-connect/all-debug.js", host.getBaseUrl());
        mvc.perform(get("/model")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['atlassian-connect-all-js-url']").value(is(expectedUrl)));
    }

    @Test
    public void shouldReturnAllJsModelAttributeForAnonymous() throws Exception {
        mvc.perform(get("/model-public")
                .param("xdm_e", "http://some-host.com")
                .param("cp", "/context-path")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['atlassian-connect-all-js-url']").value(is("http://some-host.com/context-path/atlassian-connect/all-debug.js")));
    }

    @Test
    public void shouldReturnLicenseModelAttribute() throws Exception {
        String license = "none";
        mvc.perform(get("/model-public")
                .param("lic", license)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['atlassian-connect-license']").value(is(license)));
    }

    @Test
    public void shouldReturnLocaleModelAttribute() throws Exception {
        String locale = "en-GB";
        mvc.perform(get("/model-public")
                .param("loc", locale)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['atlassian-connect-locale']").value(is(locale)));
    }

    @Test
    public void shouldReturnTimezoneModelAttribute() throws Exception {
        String timezone = "Australia/Sydney";
        mvc.perform(get("/model-public")
                .param("tz", timezone)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['atlassian-connect-timezone']").value(is(timezone)));
    }
}
