package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.LeadInquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeadInquiryRepository extends JpaRepository<LeadInquiry, UUID> {

    Optional<LeadInquiry> findByIdAndIsDeletedFalse(UUID id);

    boolean existsByLeadCodeAndIsDeletedFalse(String leadCode);

    @Query("""
            select distinct l
            from LeadInquiry l
            left join l.preferredBranch b
            left join l.course c
            left join l.batch ba
            where l.isDeleted = false
              and (:branchId is null or b.id = :branchId)
              and (:status is null or lower(l.status) = lower(:status))
              and (:leadSource is null or lower(l.leadSource) = lower(:leadSource))
              and (:courseId is null or c.id = :courseId)
              and (:batchId is null or ba.id = :batchId)
              and (
                    :searchPattern is null
                    or lower(l.leadCode) like :searchPattern
                    or lower(l.studentName) like :searchPattern
                    or lower(coalesce(l.mobileNumber, '')) like :searchPattern
                    or lower(coalesce(l.email, '')) like :searchPattern
                    or lower(coalesce(l.fatherName, '')) like :searchPattern
                    or lower(coalesce(l.motherName, '')) like :searchPattern
                    or lower(coalesce(l.guardianName, '')) like :searchPattern
              )
            """)
    Page<LeadInquiry> searchLeads(@Param("searchPattern") String searchPattern,
                                  @Param("branchId") UUID branchId,
                                  @Param("status") String status,
                                  @Param("leadSource") String leadSource,
                                  @Param("courseId") UUID courseId,
                                  @Param("batchId") UUID batchId,
                                  Pageable pageable);

    @Query("""
            select count(l)
            from LeadInquiry l
            left join l.preferredBranch b
            where l.isDeleted = false
              and (:branchId is null or b.id = :branchId)
              and (:status is null or lower(l.status) = lower(:status))
            """)
    long countByOptionalBranchAndStatus(@Param("branchId") UUID branchId, @Param("status") String status);

    @Query("""
            select count(l)
            from LeadInquiry l
            left join l.preferredBranch b
            where l.isDeleted = false
              and (:branchId is null or b.id = :branchId)
              and l.createdAt >= :from
              and l.createdAt < :to
            """)
    long countCreatedBetween(@Param("branchId") UUID branchId,
                             @Param("from") LocalDateTime from,
                             @Param("to") LocalDateTime to);
}
