package com.atlassian.connect.spring.test.util;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.auth.jwt.JwtAuthentication;
import com.atlassian.connect.spring.it.AtlassianConnectTestApplication;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.util.Optional;

@SpringApplicationConfiguration(AtlassianConnectTestApplication.class)
public class BaseApplicationTest {

    @Value("${server.port}")
    private String serverPort;

    protected MockMvc mvc;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    protected WebApplicationContext wac;

    @Before
    public void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.wac).addFilter(springSecurityFilterChain).build();
    }

    protected String getServerAddress() {
        return "http://localhost:" + serverPort;
    }

    protected void setJwtAuthenticatedPrincipal(AtlassianHost host) {
        AtlassianHostUser hostUser = new AtlassianHostUser(host, Optional.empty());
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthentication(hostUser, null));
    }
}
