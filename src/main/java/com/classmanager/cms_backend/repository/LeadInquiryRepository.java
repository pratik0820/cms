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
            left join l.createdByUser u
            where l.isDeleted = false
              and (cast(:branchId as string) is null or b.id = cast(cast(:branchId as string) as uuid))
              and (cast(:status as string) is null or lower(l.status) = lower(cast(:status as string)))
              and (cast(:leadSource as string) is null or lower(l.leadSource) = lower(cast(:leadSource as string)))
              and (cast(:courseId as string) is null or c.id = cast(cast(:courseId as string) as uuid))
              and (cast(:batchId as string) is null or ba.id = cast(cast(:batchId as string) as uuid))
              and (cast(:createdByUserId as string) is null or u.id = cast(cast(:createdByUserId as string) as uuid))
              and (
                    cast(:searchPattern as string) is null
                    or lower(l.leadCode) like cast(:searchPattern as string)
                    or lower(l.studentName) like cast(:searchPattern as string)
                    or lower(coalesce(l.mobileNumber, '')) like cast(:searchPattern as string)
                    or lower(coalesce(l.email, '')) like cast(:searchPattern as string)
                    or lower(coalesce(l.fatherName, '')) like cast(:searchPattern as string)
                    or lower(coalesce(l.motherName, '')) like cast(:searchPattern as string)
                    or lower(coalesce(l.guardianName, '')) like cast(:searchPattern as string)
              )
            """)
    Page<LeadInquiry> searchLeads(@Param("searchPattern") String searchPattern,
                                  @Param("branchId") UUID branchId,
                                  @Param("status") String status,
                                  @Param("leadSource") String leadSource,
                                  @Param("courseId") UUID courseId,
                                  @Param("batchId") UUID batchId,
                                  @Param("createdByUserId") UUID createdByUserId,
                                  Pageable pageable);

    @Query("""
            select count(l)
            from LeadInquiry l
            left join l.preferredBranch b
            left join l.createdByUser u
            where l.isDeleted = false
              and (cast(:branchId as string) is null or b.id = cast(cast(:branchId as string) as uuid))
              and (cast(:status as string) is null or lower(l.status) = lower(cast(:status as string)))
              and (cast(:createdByUserId as string) is null or u.id = cast(cast(:createdByUserId as string) as uuid))
            """)
    long countByOptionalBranchAndStatus(@Param("branchId") UUID branchId, 
                                        @Param("status") String status, 
                                        @Param("createdByUserId") UUID createdByUserId);

    @Query("""
            select count(l)
            from LeadInquiry l
            left join l.preferredBranch b
            where l.isDeleted = false
              and (cast(:branchId as string) is null or b.id = cast(cast(:branchId as string) as uuid))
              and l.createdAt >= :from
              and l.createdAt < :to
            """)
    long countCreatedBetween(@Param("branchId") UUID branchId,
                             @Param("from") LocalDateTime from,
                             @Param("to") LocalDateTime to);
}
