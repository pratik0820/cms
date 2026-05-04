package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.Phase1Record;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface Phase1RecordRepository extends JpaRepository<Phase1Record, UUID> {

    Page<Phase1Record> findByTenantIdAndModuleAndIsDeletedFalseOrderByCreatedAtDesc(
            UUID tenantId, String module, Pageable pageable);

    Page<Phase1Record> findByTenantIdAndModuleAndBranchIdAndIsDeletedFalseOrderByCreatedAtDesc(
            UUID tenantId, String module, UUID branchId, Pageable pageable);

    List<Phase1Record> findByTenantIdAndModuleAndIsDeletedFalseOrderByCreatedAtDesc(UUID tenantId, String module);

    List<Phase1Record> findByTenantIdAndModuleAndTeacherIdAndIsDeletedFalseOrderByEventDateDesc(
            UUID tenantId, String module, UUID teacherId);

    List<Phase1Record> findByTenantIdAndModuleAndStudentIdAndIsDeletedFalseOrderByEventDateDesc(
            UUID tenantId, String module, UUID studentId);

    List<Phase1Record> findByTenantIdAndModuleAndBatchIdAndIsDeletedFalseOrderByEventDateDesc(
            UUID tenantId, String module, UUID batchId);

    long countByTenantIdAndModuleAndIsDeletedFalse(UUID tenantId, String module);

    long countByTenantIdAndModuleAndStatusAndIsDeletedFalse(UUID tenantId, String module, String status);

    @Query("""
            select coalesce(sum(r.paidAmount), 0)
            from Phase1Record r
            where r.tenantId = :tenantId
              and r.module = :module
              and r.isDeleted = false
            """)
    BigDecimal sumPaid(@Param("tenantId") UUID tenantId, @Param("module") String module);

    @Query("""
            select coalesce(sum(r.pendingAmount), 0)
            from Phase1Record r
            where r.tenantId = :tenantId
              and r.module = :module
              and r.isDeleted = false
            """)
    BigDecimal sumPending(@Param("tenantId") UUID tenantId, @Param("module") String module);

    @Query("""
            select count(r)
            from Phase1Record r
            where r.tenantId = :tenantId
              and r.module = :module
              and r.eventDate between :fromDate and :toDate
              and r.isDeleted = false
            """)
    long countBetween(@Param("tenantId") UUID tenantId,
                      @Param("module") String module,
                      @Param("fromDate") LocalDate fromDate,
                      @Param("toDate") LocalDate toDate);
}
