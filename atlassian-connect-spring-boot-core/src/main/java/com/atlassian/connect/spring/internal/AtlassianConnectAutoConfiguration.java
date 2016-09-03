package com.atlassian.connect.spring.internal;

import com.atlassian.connect.spring.internal.auth.jwt.JwtAuthenticationFilter;
import com.atlassian.connect.spring.internal.descriptor.AddonDescriptorLoader;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationManager;

import javax.servlet.Filter;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Atlassian Connect add-ons.
 */
@Configuration
@ComponentScan(basePackageClasses = {AtlassianConnectAutoConfiguration.class})
@EnableConfigurationProperties(AtlassianConnectProperties.class)
@ConditionalOnResource(resources = AddonDescriptorLoader.DESCRIPTOR_RESOURCE_PATH)
@EnableAsync
public class AtlassianConnectAutoConfiguration {

    @Configuration
    @PropertySource("classpath:config/default.properties")
    static class Defaults {}

    @Configuration
    @Profile("production")
    @PropertySource({"classpath:config/default.properties", "classpath:config/production.properties"})
    static class Production {}

    @Bean
    public Filter jwtAuthenticationFilter(AuthenticationManager authenticationManager, ServerProperties serverProperties) {
        return new JwtAuthenticationFilter(authenticationManager, serverProperties);
    }
}
