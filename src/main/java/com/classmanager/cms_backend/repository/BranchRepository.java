package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID> {

    List<Branch> findByIsActiveTrueAndIsDeletedFalseOrderByNameAsc();

    List<Branch> findByTenantIdAndIsDeletedFalseOrderByNameAsc(UUID tenantId);

    Optional<Branch> findByIdAndIsDeletedFalse(UUID id);

    Optional<Branch> findByIdAndTenantIdAndIsDeletedFalse(UUID id, UUID tenantId);

    boolean existsByNameAndTenantIdAndIsDeletedFalse(String name, UUID tenantId);

    long countByTenantIdAndIsDeletedFalse(UUID tenantId);
}
