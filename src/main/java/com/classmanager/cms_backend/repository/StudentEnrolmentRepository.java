package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.StudentEnrolment;
import com.classmanager.cms_backend.enums.EnrolmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentEnrolmentRepository extends JpaRepository<StudentEnrolment, UUID> {

    Optional<StudentEnrolment> findByIdAndIsDeletedFalse(UUID id);

    List<StudentEnrolment> findByStudent_IdAndIsDeletedFalseOrderByEnrolmentDateDesc(UUID studentId);

    Page<StudentEnrolment> findByBatch_IdAndIsDeletedFalseOrderByStudent_NameAsc(UUID batchId, Pageable pageable);

    boolean existsByStudent_IdAndBatch_IdAndStatusAndIsDeletedFalse(
            UUID studentId, UUID batchId, EnrolmentStatus status);

    @Query("""
            SELECT e FROM StudentEnrolment e
            WHERE e.batch.branch.id = :branchId
              AND e.academicYear = :academicYear
              AND e.isDeleted = false
            ORDER BY e.student.name ASC
            """)
    Page<StudentEnrolment> findByBranchAndYear(
            @Param("branchId") UUID branchId,
            @Param("academicYear") String academicYear,
            Pageable pageable);

    long countByBatch_IdAndStatusAndIsDeletedFalse(UUID batchId, EnrolmentStatus status);
}
