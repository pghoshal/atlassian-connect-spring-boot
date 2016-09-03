package com.atlassian.connect.spring.it;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestRepositoryConsumer {

    @Autowired
    private TestRepository testRepository;
}
