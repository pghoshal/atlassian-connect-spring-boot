package com.atlassian.connect.spring.test.configuration;

import com.atlassian.connect.spring.test.util.BaseApplicationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("config-test")
@WebAppConfiguration
public class SpringPropertiesFileTest extends BaseApplicationTest {
    
    @Value("${management.context-path}")
    private String managementContextPath;
    
    @Test
    public void shouldOverrideDefaultConfigValue() {
        assertEquals(managementContextPath, "/override");
    }
}
