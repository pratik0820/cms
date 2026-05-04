package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, UUID> {

    Optional<Teacher> findByIdAndIsDeletedFalse(UUID id);

    Optional<Teacher> findByIdAndTenantIdAndIsDeletedFalse(UUID id, UUID tenantId);

    Optional<Teacher> findByUserIdAndIsDeletedFalse(UUID userId);

    Page<Teacher> findByBranchIdAndIsDeletedFalseOrderByNameAsc(UUID branchId, Pageable pageable);

    Page<Teacher> findByIsDeletedFalseOrderByNameAsc(Pageable pageable);

    Page<Teacher> findByTenantIdAndIsDeletedFalseOrderByNameAsc(UUID tenantId, Pageable pageable);

    boolean existsByEmailAndTenantIdAndIsDeletedFalse(String email, UUID tenantId);

    long countByTenantIdAndIsDeletedFalse(UUID tenantId);
}
