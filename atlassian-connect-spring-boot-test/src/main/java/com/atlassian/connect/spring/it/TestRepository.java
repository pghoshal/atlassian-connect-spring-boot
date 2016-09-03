package com.atlassian.connect.spring.it;

import org.springframework.data.repository.CrudRepository;

public interface TestRepository extends CrudRepository<TestEntity, String> {}
