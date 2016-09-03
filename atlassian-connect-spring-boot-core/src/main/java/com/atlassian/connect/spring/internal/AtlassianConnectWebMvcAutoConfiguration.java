package com.atlassian.connect.spring.internal;

import com.atlassian.connect.spring.internal.auth.RequireAuthenticationHandlerInterceptor;
import com.atlassian.connect.spring.internal.descriptor.AddonDescriptorLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * A Spring Web MVC configuration class, providing custom functionality for Atlassian Connect.
 */
@Configuration
@ConditionalOnResource(resources = AddonDescriptorLoader.DESCRIPTOR_RESOURCE_PATH)
public class AtlassianConnectWebMvcAutoConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    private RequireAuthenticationHandlerInterceptor requireAuthenticationHandlerInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requireAuthenticationHandlerInterceptor);
    }
}
