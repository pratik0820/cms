package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.BatchSubtopicProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BatchSubtopicProgressRepository extends JpaRepository<BatchSubtopicProgress, UUID> {

    List<BatchSubtopicProgress> findByBatchId(UUID batchId);

    Optional<BatchSubtopicProgress> findByBatchIdAndSubtopicId(UUID batchId, UUID subtopicId);

    @Query("SELECT bsp FROM BatchSubtopicProgress bsp " +
           "JOIN FETCH bsp.batch b " +
           "JOIN FETCH bsp.subtopic st " +
           "JOIN FETCH st.chapter c " +
           "JOIN FETCH c.subject s " +
           "LEFT JOIN FETCH bsp.createdByUser u " +
           "WHERE (:branchId IS NULL OR b.branch.id = :branchId) " +
           "AND (:standard IS NULL OR b.course.standard = :standard) " +
           "AND (:subjectId IS NULL OR s.id = :subjectId) " +
           "AND (:teacherId IS NULL OR u.id = :teacherId) " + // Note: approximate teacher filter based on createdByUser
           "AND (:status IS NULL OR bsp.status = :status) " +
           "AND (:startDate IS NULL OR cast(bsp.updatedAt as date) >= :startDate) " +
           "AND (:endDate IS NULL OR cast(bsp.updatedAt as date) <= :endDate) " +
           "AND bsp.isDeleted = false AND b.isDeleted = false AND st.isDeleted = false AND c.isDeleted = false")
    List<BatchSubtopicProgress> findAllDashboardData(
            @Param("branchId") UUID branchId,
            @Param("standard") String standard,
            @Param("subjectId") UUID subjectId,
            @Param("teacherId") UUID teacherId,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
           SELECT bsp FROM BatchSubtopicProgress bsp
           JOIN FETCH bsp.batch b
           JOIN FETCH bsp.subtopic st
           LEFT JOIN FETCH bsp.createdByUser u
           WHERE b.id IN :batchIds AND bsp.isDeleted = false
           """)
    List<BatchSubtopicProgress> findProgressForBatches(@Param("batchIds") List<UUID> batchIds);

}
