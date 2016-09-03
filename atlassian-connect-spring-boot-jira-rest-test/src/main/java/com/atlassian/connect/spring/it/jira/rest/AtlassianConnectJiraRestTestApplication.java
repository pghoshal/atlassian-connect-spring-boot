package com.atlassian.connect.spring.it.jira.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AtlassianConnectJiraRestTestApplication {

    public static void main(String[] args) throws Exception {
        new SpringApplication(AtlassianConnectJiraRestTestApplication.class).run(args);
    }
}
