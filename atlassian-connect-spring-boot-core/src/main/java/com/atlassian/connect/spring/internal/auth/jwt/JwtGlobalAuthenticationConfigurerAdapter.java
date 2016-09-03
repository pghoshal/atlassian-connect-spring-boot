package com.atlassian.connect.spring.internal.auth.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;

/**
 * A configuration class responsible for registering a {@link JwtAuthenticationProvider} without interfering with
 * auto-configured Basic authentication support.
 */
@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
public class JwtGlobalAuthenticationConfigurerAdapter extends GlobalAuthenticationConfigurerAdapter {

    @Autowired
    private JwtAuthenticationProvider jwtAuthenticationProvider;

    @Override
    public void init(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.apply(new JwtSecurityConfigurer(jwtAuthenticationProvider));
    }

    private static class JwtSecurityConfigurer implements SecurityConfigurer<AuthenticationManager, AuthenticationManagerBuilder> {

        private final JwtAuthenticationProvider jwtAuthenticationProvider;

        public JwtSecurityConfigurer(JwtAuthenticationProvider jwtAuthenticationProvider) {
            this.jwtAuthenticationProvider = jwtAuthenticationProvider;
        }

        @Override
        public void init(AuthenticationManagerBuilder builder) throws Exception {}

        @Override
        public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
            authenticationManagerBuilder.authenticationProvider(jwtAuthenticationProvider);
        }
    }
}
