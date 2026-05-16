package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.Batch;
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
public interface BatchRepository extends JpaRepository<Batch, UUID> {

    List<Batch> findByBranch_IdAndIsActiveTrueAndIsDeletedFalseOrderByNameAsc(UUID branchId);

    List<Batch> findByBranch_IdAndAcademicYearAndIsActiveTrueAndIsDeletedFalseOrderByNameAsc(
            UUID branchId, String academicYear);

    List<Batch> findByCourse_IdAndIsActiveTrueAndIsDeletedFalseOrderByNameAsc(UUID courseId);

    Page<Batch> findByBranch_IdAndIsDeletedFalseOrderByNameAsc(UUID branchId, Pageable pageable);

    Page<Batch> findByIsDeletedFalseOrderByNameAsc(Pageable pageable);

    Optional<Batch> findByIdAndIsDeletedFalse(UUID id);

    Optional<Batch> findByIdAndIsActiveTrueAndIsDeletedFalse(UUID id);

    long countByBranch_IdAndIsActiveTrueAndIsDeletedFalse(UUID branchId);

    @Query("""
            SELECT b FROM Batch b
            WHERE b.branch.id = :branchId
              AND b.course.id = :courseId
              AND b.academicYear = :academicYear
              AND b.isDeleted = false
            """)
    List<Batch> findByBranchCourseAndYear(
            @Param("branchId") UUID branchId,
            @Param("courseId") UUID courseId,
            @Param("academicYear") String academicYear);
}
