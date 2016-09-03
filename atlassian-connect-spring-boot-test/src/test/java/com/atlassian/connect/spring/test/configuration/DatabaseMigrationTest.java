package com.atlassian.connect.spring.test.configuration;

import com.atlassian.connect.spring.it.TestRepository;
import com.atlassian.connect.spring.test.util.BaseApplicationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class DatabaseMigrationTest extends BaseApplicationTest {
    
    @Autowired
    private TestRepository testRepository;
    
    @Test
    public void canUseRepositoryAfterFlywayMigration() {
        assertThat(testRepository.count(), is(0L));
    }
}
