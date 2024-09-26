package com.backend.demo.repository;

import com.backend.demo.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Integer> {
    Optional<Organization> findByOrganizationName(String name);
}