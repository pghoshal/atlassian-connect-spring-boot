package com.atlassian.connect.spring.it;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackageClasses = {TestRepository.class})
public class AtlassianConnectTestApplication {

    public static void main(String[] args) throws Exception {
        new SpringApplication(AtlassianConnectTestApplication.class).run(args);
    }
}
