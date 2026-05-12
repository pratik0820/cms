package com.classmanager.cms_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "generated_reports", indexes = {
        @Index(name = "idx_generated_reports_category", columnList = "category"),
        @Index(name = "idx_generated_reports_generated_on", columnList = "generated_on"),
        @Index(name = "idx_generated_reports_branch", columnList = "branch_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratedReport extends BaseEntity {

    @Column(name = "report_name", nullable = false)
    private String reportName;

    @Column(name = "category", nullable = false, length = 80)
    private String category;

    @Column(name = "report_type", nullable = false, length = 120)
    private String reportType;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "format", nullable = false, length = 30)
    private String format;

    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "filters_json", columnDefinition = "TEXT")
    private String filtersJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by_user_id")
    private User generatedByUser;

    @Column(name = "generated_on", nullable = false)
    private LocalDateTime generatedOn;

    @Column(name = "storage_provider", length = 40)
    private String storageProvider;

    @Column(name = "storage_key", length = 1000)
    private String storageKey;

    @Column(name = "download_url", length = 1000)
    private String downloadUrl;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
