package com.atlassian.connect.spring.internal.jpa;

import com.atlassian.connect.spring.AtlassianHostRepository;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Atlassian Connect add-ons using Spring Data JPA and Liquibase.
 */
@Configuration
@ConditionalOnResource(resources = "classpath:atlassian-connect.json")
@AutoConfigureAfter({ DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
@AutoConfigureBefore(LiquibaseAutoConfiguration.class)
@EnableJpaRepositories(basePackageClasses = {AtlassianHostRepository.class})
@EnableJpaAuditing
@PropertySource("classpath:config/atlassian-connect-spring-boot-jpa-starter.properties")
public class AtlassianJpaAutoConfiguration {}
