package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.LeadFollowUp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeadFollowUpRepository extends JpaRepository<LeadFollowUp, UUID> {

    List<LeadFollowUp> findByLead_IdAndIsDeletedFalseOrderByFollowUpAtDesc(UUID leadId);

    Optional<LeadFollowUp> findByIdAndIsDeletedFalse(UUID id);

    @Query("""
            select f from LeadFollowUp f
            join f.lead l
            left join f.createdByUser u
            left join l.preferredBranch b
            where f.isDeleted = false
              and (:branchId is null or b.id = :branchId)
              and (
                    cast(:searchPattern as string) is null
                    or lower(l.studentName) like cast(:searchPattern as string)
                    or lower(l.leadCode) like cast(:searchPattern as string)
              )
              and (:userId is null or u.id = :userId)
              and (:modeOfContact is null or lower(f.modeOfContact) = lower(cast(:modeOfContact as string)))
              and (cast(:startDate as timestamp) is null or f.followUpAt >= :startDate)
              and (cast(:endDate as timestamp) is null or f.followUpAt <= :endDate)
              and (
                    :status is null
                    or (:status = 'COMPLETED' and lower(f.statusAfter) = 'completed')
                    or (:status = 'PENDING' and lower(f.statusAfter) = 'pending' and (f.nextFollowUpAt is null or f.nextFollowUpAt >= :now))
                    or (:status = 'OVERDUE' and lower(f.statusAfter) = 'pending' and f.nextFollowUpAt < :now)
              )
            """)
    Page<LeadFollowUp> searchFollowUps(
            @Param("branchId") UUID branchId,
            @Param("searchPattern") String searchPattern,
            @Param("userId") UUID userId,
            @Param("modeOfContact") String modeOfContact,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") String status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Query("""
            select count(f) from LeadFollowUp f
            join f.lead l
            left join f.createdByUser u
            left join l.preferredBranch b
            where f.isDeleted = false
              and (:branchId is null or b.id = :branchId)
              and (
                    cast(:searchPattern as string) is null
                    or lower(l.studentName) like cast(:searchPattern as string)
                    or lower(l.leadCode) like cast(:searchPattern as string)
              )
              and (:userId is null or u.id = :userId)
              and (:modeOfContact is null or lower(f.modeOfContact) = lower(cast(:modeOfContact as string)))
              and (cast(:startDate as timestamp) is null or f.followUpAt >= :startDate)
              and (cast(:endDate as timestamp) is null or f.followUpAt <= :endDate)
              and (
                    :status is null
                    or (:status = 'COMPLETED' and lower(f.statusAfter) = 'completed')
                    or (:status = 'PENDING' and lower(f.statusAfter) = 'pending')
              )
            """)
    long countFollowUps(
            @Param("branchId") UUID branchId,
            @Param("searchPattern") String searchPattern,
            @Param("userId") UUID userId,
            @Param("modeOfContact") String modeOfContact,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") String status
    );

    @Query("""
            select count(distinct l.id) from LeadFollowUp f
            join f.lead l
            left join f.createdByUser u
            left join l.preferredBranch b
            where f.isDeleted = false
              and l.status = 'CONVERTED'
              and (:branchId is null or b.id = :branchId)
              and (
                    cast(:searchPattern as string) is null
                    or lower(l.studentName) like cast(:searchPattern as string)
                    or lower(l.leadCode) like cast(:searchPattern as string)
              )
              and (:userId is null or u.id = :userId)
              and (:modeOfContact is null or lower(f.modeOfContact) = lower(cast(:modeOfContact as string)))
              and (cast(:startDate as timestamp) is null or f.followUpAt >= :startDate)
              and (cast(:endDate as timestamp) is null or f.followUpAt <= :endDate)
            """)
    long countConvertedLeads(
            @Param("branchId") UUID branchId,
            @Param("searchPattern") String searchPattern,
            @Param("userId") UUID userId,
            @Param("modeOfContact") String modeOfContact,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
