package com.atlassian.connect.spring.test.lifecycle;

import com.atlassian.connect.spring.AddonInstalledEvent;
import com.atlassian.connect.spring.AddonUninstalledEvent;
import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.it.AddonLifecycleEventCollector;
import com.atlassian.connect.spring.test.util.AtlassianHosts;
import com.atlassian.connect.spring.test.util.BaseApplicationTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class LifecycleControllerTest extends BaseApplicationTest {

    private static final String CLIENT_KEY = "some-host";

    private static final String SHARED_SECRET = "some-secret";

    private static final String BASE_URL = "http://example.com";

    @Value("${security.user.name}")
    private String adminUserName;

    @Value("${security.user.password}")
    private String adminUserPassword;

    @Autowired
    private AtlassianHostRepository hostRepository;

    @Autowired
    private AddonLifecycleEventCollector addonLifecycleEventCollector;

    @Value("${hello-world.installed-url}")
    private String installedUrl;

    @Value("${hello-world.uninstalled-url}")
    private String uninstalledUrl;

    @Before
    public void setUp() {
        addonLifecycleEventCollector.clearEvents();
    }

    @Test
    public void shouldStoreHostOnFirstInstall() throws Exception {
        mvc.perform(postInstalled(installedUrl, SHARED_SECRET))
                .andExpect(status().isNoContent());

        AtlassianHost installedHost = hostRepository.findOne(CLIENT_KEY);
        assertThat(installedHost, notNullValue());
        assertThat(installedHost.getSharedSecret(), is(SHARED_SECRET));
        assertThat(installedHost.getBaseUrl(), is(AtlassianHosts.BASE_URL));
        assertThat(installedHost.isAddonInstalled(), is(true));
    }

    @Test
    public void shouldFireEventOnInstall() throws Exception {
        mvc.perform(postInstalled(installedUrl, SHARED_SECRET))
                .andExpect(status().isNoContent());

        AtlassianHost installedHost = hostRepository.findOne(CLIENT_KEY);
        AddonInstalledEvent installedEvent = addonLifecycleEventCollector.takeInstalledEvent();
        assertThat(installedEvent.getHost(), equalTo(installedHost));
    }

    @Test
    public void shouldUpdateSharedSecretOnSignedSecondInstall() throws Exception {
        AtlassianHost host = saveHost(CLIENT_KEY, SHARED_SECRET, BASE_URL);
        String newSharedSecret = "some-other-secret";
        setJwtAuthenticatedPrincipal(host);
        mvc.perform(postInstalled(installedUrl, newSharedSecret))
                .andExpect(status().isNoContent());

        AtlassianHost installedHost = hostRepository.findOne(CLIENT_KEY);
        assertThat(installedHost, notNullValue());
        assertThat(installedHost.getSharedSecret(), is(newSharedSecret));
    }

    @Test
    public void shouldRejectSecondInstallWithoutJwt() throws Exception {
        saveHost(CLIENT_KEY, SHARED_SECRET, BASE_URL);
        mvc.perform(postInstalled(installedUrl, SHARED_SECRET))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldRejectSharedSecretUpdateByOtherHost() throws Exception {
        saveHost(CLIENT_KEY, SHARED_SECRET, BASE_URL);
        AtlassianHost otherHost = saveHost("other-host", "other-secret", "http://other-example.com");
        setJwtAuthenticatedPrincipal(otherHost);
        mvc.perform(postInstalled(installedUrl, "some-other-secret")
                .with(httpBasic(adminUserName, adminUserPassword)))
                .andExpect(status().isForbidden());    }

    @Test
    public void shouldRejectUninstallByOtherHost() throws Exception {
        saveHost(CLIENT_KEY, SHARED_SECRET, BASE_URL);
        AtlassianHost otherHost = saveHost("other-host", "other-secret", "http://other-example.com");
        setJwtAuthenticatedPrincipal(otherHost);
        mvc.perform(postUninstalled(uninstalledUrl, SHARED_SECRET)
                .with(httpBasic(adminUserName, adminUserPassword)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldSoftDeleteHostOnUninstall() throws Exception {
        AtlassianHost host = saveHost(CLIENT_KEY, SHARED_SECRET, BASE_URL);
        setJwtAuthenticatedPrincipal(host);
        mvc.perform(postUninstalled(uninstalledUrl, SHARED_SECRET))
                .andExpect(status().isNoContent());

        AtlassianHost installedHost = hostRepository.findOne(CLIENT_KEY);
        assertThat(installedHost, notNullValue());
        assertThat(installedHost.getSharedSecret(), is(SHARED_SECRET));
        assertThat(installedHost.getBaseUrl(), is(BASE_URL));
        assertThat(installedHost.isAddonInstalled(), is(false));
    }

    @Test
    public void shouldFireEventOnUninstall() throws Exception {
        AtlassianHost host = saveHost(CLIENT_KEY, SHARED_SECRET, BASE_URL);
        setJwtAuthenticatedPrincipal(host);
        mvc.perform(postUninstalled(uninstalledUrl, SHARED_SECRET))
                .andExpect(status().isNoContent());

        AtlassianHost installedHost = hostRepository.findOne(CLIENT_KEY);
        AddonUninstalledEvent uninstalledEvent = addonLifecycleEventCollector.takeUninstalledEvent();
        assertThat(uninstalledEvent.getHost(), equalTo(installedHost));
    }

    @Test
    public void shouldIgnoreMissingHostOnUninstall() throws Exception {
        mvc.perform(postUninstalled(uninstalledUrl, SHARED_SECRET))
                .andExpect(status().isNoContent());

        AtlassianHost installedHost = hostRepository.findOne(CLIENT_KEY);
        assertThat(installedHost, nullValue());
    }

    @Test
    public void shouldRejectSharedSecretUpdateWithBasicAuth() throws Exception {
        saveHost(CLIENT_KEY, SHARED_SECRET, BASE_URL);
        mvc.perform(postInstalled(installedUrl, "some-other-secret")
                .with(httpBasic(adminUserName, adminUserPassword)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldRejectUninstallWithBasicAuth() throws Exception {
        saveHost(CLIENT_KEY, SHARED_SECRET, BASE_URL);
        mvc.perform(postUninstalled(uninstalledUrl, SHARED_SECRET)
                .with(httpBasic(adminUserName, adminUserPassword)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldRejectInstallWithoutBody() throws Exception {
        mvc.perform(post(installedUrl)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRejectInstallWithInvalidBody() throws Exception {
        mvc.perform(post(installedUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRejectInstallWithInvalidEventType() throws Exception {
        mvc.perform(post(installedUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createLifecycleJson("uninstalled", "some-secret")))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRejectUninstallWithoutBody() throws Exception {
        mvc.perform(post(uninstalledUrl)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRejectUninstallWithInvalidBody() throws Exception {
        mvc.perform(post(uninstalledUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRejectUninstallWithInvalidEventType() throws Exception {
        mvc.perform(post(uninstalledUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createLifecycleJson("installed", "some-secret")))
                .andExpect(status().isBadRequest());
    }

    private MockHttpServletRequestBuilder postInstalled(String path, String secret) throws Exception {
        return postLifecycleCallback(path, "installed", secret);
    }

    private MockHttpServletRequestBuilder postUninstalled(String path, String secret) throws Exception {
        return postLifecycleCallback(path, "uninstalled", secret);
    }

    private MockHttpServletRequestBuilder postLifecycleCallback(String path, String eventType, String secret) throws JsonProcessingException {
        return post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createLifecycleJson(eventType, secret));
    }

    private String createLifecycleJson(String eventType, String secret) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(LifecycleBodyHelper.createLifecycleEventMap(eventType, secret));
    }

    private AtlassianHost saveHost(String clientKey, String sharedSecret, String baseUrl) {
        AtlassianHost host = new AtlassianHost();
        host.setClientKey(clientKey);
        host.setSharedSecret(sharedSecret);
        host.setBaseUrl(baseUrl);
        hostRepository.save(host);
        return host;
    }

    @After
    public void cleanup() {
        hostRepository.deleteAll();
    }
}
