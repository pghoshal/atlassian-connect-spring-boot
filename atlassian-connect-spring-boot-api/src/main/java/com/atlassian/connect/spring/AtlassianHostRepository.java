package com.atlassian.connect.spring;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * A Spring Data repository for storing information about Atlassian host applications in which the add-on is or has been
 * installed.
 */
public interface AtlassianHostRepository extends CrudRepository<AtlassianHost, String> {

    /**
     * Returns the host with the given base URL.
     *
     * @param baseUrl the base URL of the host application
     * @return the host with the given base URL or {@link Optional#empty()}
     */
    Optional<AtlassianHost> findFirstByBaseUrl(String baseUrl);
}
