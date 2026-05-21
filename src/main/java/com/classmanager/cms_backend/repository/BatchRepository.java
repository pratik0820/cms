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

    @Query("""
            select b from Batch b
            join b.course c
            left join c.standardRef s
            where b.isDeleted = false
              and (:branchId is null or b.branch.id = :branchId)
              and (
                    :search is null
                    or lower(b.displayCode) like lower(concat('%', :search, '%'))
                    or lower(b.name) like lower(concat('%', :search, '%'))
                    or lower(c.name) like lower(concat('%', :search, '%'))
                    or lower(c.standard) like lower(concat('%', :search, '%'))
                    or lower(c.medium) like lower(concat('%', :search, '%'))
                    or lower(cast(c.board as string)) like lower(concat('%', :search, '%'))
              )
            """)
    Page<Batch> searchAcademicCourses(@Param("branchId") UUID branchId,
                                      @Param("search") String search,
                                      Pageable pageable);

    @Query("""
            select distinct b from Batch b
            join fetch b.course c
            left join fetch c.standardRef s
            left join fetch c.subjects subjects
            where b.id = :batchId
              and b.isDeleted = false
            """)
    Optional<Batch> findAcademicCourseDetail(@Param("batchId") UUID batchId);

    boolean existsByDisplayCode(String displayCode);

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
