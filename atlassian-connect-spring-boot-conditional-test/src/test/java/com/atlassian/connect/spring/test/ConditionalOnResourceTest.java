package com.atlassian.connect.spring.test;

import com.atlassian.connect.spring.it.conditional.DescriptorLessApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(DescriptorLessApplication.class)
@WebAppConfiguration
@DirtiesContext
public class ConditionalOnResourceTest {

    @Autowired
    protected WebApplicationContext wac;

    @Test
    public void shouldNotRegisterAnyBeansWithoutAddonDescriptorPresent() {
        assertThat(getStarterBeanClasses().toArray(), is(new Object[] {}));
    }

    private List<? extends Class<?>> getStarterBeanClasses() {
        return Arrays.asList(wac.getBeanDefinitionNames()).stream()
                .map(wac::getType)
                .filter(this::isStarterClass)
                .collect(Collectors.toList());
    }

    private boolean isStarterClass(Class c) {
        Package classPackage = c.getPackage();
        return classPackage != null
                && classPackage.getName().startsWith("com.atlassian.connect.spring")
                && !classPackage.getName().equals("com.atlassian.connect.spring.it.conditional");
    }
}

