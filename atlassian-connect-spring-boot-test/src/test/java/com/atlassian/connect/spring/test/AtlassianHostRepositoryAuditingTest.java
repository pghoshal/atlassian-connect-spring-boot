package com.atlassian.connect.spring.test;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.auth.jwt.JwtAuthentication;
import com.atlassian.connect.spring.test.util.AtlassianHosts;
import com.atlassian.connect.spring.test.util.BaseApplicationTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class AtlassianHostRepositoryAuditingTest extends BaseApplicationTest {

    private static final String USER_KEY = "charlie";

    @Autowired
    private AtlassianHostRepository hostRepository;

    @Before
    public void setUp() {
        AtlassianHostUser hostUser = new AtlassianHostUser(null, Optional.of(USER_KEY));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthentication(hostUser, null));
    }

    @Test
    public void shouldStoreAuditingFields() {
        AtlassianHost host = new AtlassianHost();
        host.setClientKey(randomAlphanumeric(32));
        host.setSharedSecret(AtlassianHosts.SHARED_SECRET);
        host.setBaseUrl(AtlassianHosts.BASE_URL);
        host.setProductType("some-product");
        hostRepository.save(host);

        AtlassianHost readHost = hostRepository.findOne(host.getClientKey());
        assertThat(readHost.getCreatedDate(), is(notNullValue()));
        assertThat(readHost.getCreatedBy(), is(notNullValue()));
        assertThat(readHost.getLastModifiedDate(), is(notNullValue()));
        assertThat(readHost.getLastModifiedBy(), is(notNullValue()));
    }
}
