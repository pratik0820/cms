package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BatchRepository extends JpaRepository<Batch, UUID> {

    List<Batch> findByTenantIdAndIsDeletedFalseOrderByNameAsc(UUID tenantId);

    List<Batch> findByTenantIdAndBranchIdAndIsDeletedFalseOrderByNameAsc(UUID tenantId, UUID branchId);

    Optional<Batch> findByIdAndTenantIdAndIsDeletedFalse(UUID id, UUID tenantId);
}
