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

    Page<Student> findByBranch_IdAndIsDeletedFalseOrderByNameAsc(UUID branchId, Pageable pageable);

    Page<Student> findByIsDeletedFalseOrderByNameAsc(Pageable pageable);

    long countByIsDeletedFalse();

    long countByBranch_IdAndIsDeletedFalse(UUID branchId);

    long countByIsActiveTrueAndIsDeletedFalse();

    long countByCreatedAtBetweenAndIsDeletedFalse(java.time.LocalDateTime from, java.time.LocalDateTime to);

    long countByBranch_IdAndCreatedAtBetweenAndIsDeletedFalse(UUID branchId,
                                                              java.time.LocalDateTime from,
                                                              java.time.LocalDateTime to);

    long countByCreatedAtBeforeAndIsDeletedFalse(java.time.LocalDateTime before);

    long countByBranch_IdAndCreatedAtBeforeAndIsDeletedFalse(UUID branchId, java.time.LocalDateTime before);

    @Query("SELECT s FROM Student s WHERE s.isAdmissionFinal = false AND s.isDeleted = false")
    Page<Student> findDraftAdmissions(Pageable pageable);
}
