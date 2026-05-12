package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.GeneratedReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GeneratedReportRepository extends JpaRepository<GeneratedReport, UUID> {

    Optional<GeneratedReport> findByIdAndIsDeletedFalse(UUID id);

    @Query("""
            select gr
            from GeneratedReport gr
            left join gr.branch b
            where gr.isDeleted = false
              and (:category is null or lower(gr.category) = lower(:category))
              and (:reportType is null or lower(gr.reportType) = lower(:reportType))
              and (:format is null or lower(gr.format) = lower(:format))
              and (:branchId is null or b.id = :branchId)
              and (:fromDate is null or gr.generatedOn >= :fromDateStart)
              and (:toDate is null or gr.generatedOn < :toDateExclusive)
              and (
                    :searchPattern is null
                    or lower(gr.reportName) like :searchPattern
                    or lower(coalesce(gr.description, '')) like :searchPattern
              )
            """)
    Page<GeneratedReport> searchReports(@Param("category") String category,
                                        @Param("reportType") String reportType,
                                        @Param("format") String format,
                                        @Param("branchId") UUID branchId,
                                        @Param("fromDate") LocalDate fromDate,
                                        @Param("toDate") LocalDate toDate,
                                        @Param("fromDateStart") java.time.LocalDateTime fromDateStart,
                                        @Param("toDateExclusive") java.time.LocalDateTime toDateExclusive,
                                        @Param("searchPattern") String searchPattern,
                                        Pageable pageable);

    long countByIsDeletedFalse();
}
