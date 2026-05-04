package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    Optional<Student> findByIdAndIsDeletedFalse(UUID id);

    Optional<Student> findByIdAndTenantIdAndIsDeletedFalse(UUID id, UUID tenantId);

    Optional<Student> findByLoginIdAndIsDeletedFalse(String loginId);

    Page<Student> findByBranchIdAndIsDeletedFalseOrderByNameAsc(UUID branchId, Pageable pageable);

    Page<Student> findByBatchIdAndIsDeletedFalseOrderByNameAsc(UUID batchId, Pageable pageable);

    Page<Student> findByIsDeletedFalseOrderByNameAsc(Pageable pageable);

    Page<Student> findByTenantIdAndIsDeletedFalseOrderByNameAsc(UUID tenantId, Pageable pageable);

    boolean existsByLoginIdAndIsDeletedFalse(String loginId);

    long countByTenantIdAndIsDeletedFalse(UUID tenantId);

    long countByBranchIdAndIsDeletedFalse(UUID branchId);

    @Query("SELECT s FROM Student s WHERE s.isAdmissionFinal = false AND s.isDeleted = false")
    Page<Student> findDraftAdmissions(Pageable pageable);
}
