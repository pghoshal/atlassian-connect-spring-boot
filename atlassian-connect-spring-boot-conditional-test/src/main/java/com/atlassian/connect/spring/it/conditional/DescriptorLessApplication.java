package com.atlassian.connect.spring.it.conditional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DescriptorLessApplication {

    public static void main(String[] args) throws Exception {
        new SpringApplication(DescriptorLessApplication.class).run(args);
    }
}
