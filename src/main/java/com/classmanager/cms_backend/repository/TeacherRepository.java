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

    Optional<Teacher> findByUser_IdAndIsDeletedFalse(UUID userId);

    Page<Teacher> findByBranch_IdAndIsDeletedFalseOrderByNameAsc(UUID branchId, Pageable pageable);

    Page<Teacher> findByIsDeletedFalseOrderByNameAsc(Pageable pageable);

    long countByIsDeletedFalse();

    long countByBranch_IdAndIsDeletedFalse(UUID branchId);

    long countByIsActiveTrueAndIsDeletedFalse();

    long countByBranch_IdAndIsActiveTrueAndIsDeletedFalse(UUID branchId);

    long countByCreatedAtBetweenAndIsDeletedFalse(java.time.LocalDateTime from, java.time.LocalDateTime to);

    long countByBranch_IdAndCreatedAtBetweenAndIsDeletedFalse(UUID branchId,
                                                              java.time.LocalDateTime from,
                                                              java.time.LocalDateTime to);
}
