package com.atlassian.connect.spring.internal;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AtlassianConnectContextModelAttributeProviderTest {

    private static final String HOSTNAME = "http://context-host.com";

    private static final String CONTEXT_PATH = "/product";

    @Mock
    private HttpServletRequest request;

    @Mock
    private AtlassianConnectProperties atlassianConnectProperties;

    @InjectMocks
    private AtlassianConnectContextModelAttributeProvider modelAttributeProvider;

    @Test
    public void shouldReturnEmptyAllJsUrlForUnauthenticatedIfContextMissing() {
        assertThat(modelAttributeProvider.getAllJsUrl(), is(""));
    }

    @Test
    public void shouldReturnAllJsUrlForUnauthenticatedWithOnlyHost() {
        when(request.getParameter("xdm_e")).thenReturn(HOSTNAME);
        assertThat(modelAttributeProvider.getAllJsUrl(), is("http://context-host.com/atlassian-connect/all.js"));
    }

    @Test
    public void shouldReturnAllJsUrlForUnauthenticatedWithHostAndContextPath() {
        when(request.getParameter("xdm_e")).thenReturn(HOSTNAME);
        when(request.getParameter("cp")).thenReturn(CONTEXT_PATH);
        assertThat(modelAttributeProvider.getAllJsUrl(), is("http://context-host.com/product/atlassian-connect/all.js"));
    }

    @Test
    public void shouldReturnAllJsUrlFromHostForAuthenticated() {
        AtlassianHost mockHost = mock(AtlassianHost.class);
        when(mockHost.getBaseUrl()).thenReturn("http://stored-host.com");

        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getPrincipal()).thenReturn(new AtlassianHostUser(mockHost, Optional.empty()));
        when(request.getUserPrincipal()).thenReturn(mockAuthentication);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(mockAuthentication);
        SecurityContextHolder.setContext(securityContext);

        assertThat(modelAttributeProvider.getAllJsUrl(), is("http://stored-host.com/atlassian-connect/all.js"));
    }
}
