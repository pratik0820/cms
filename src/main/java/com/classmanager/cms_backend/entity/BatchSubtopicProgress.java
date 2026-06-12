package com.classmanager.cms_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "batch_subtopic_progress", indexes = {
        @Index(name = "idx_batch_subtopic_progress_batch", columnList = "batch_id"),
        @Index(name = "idx_batch_subtopic_progress_subtopic", columnList = "subtopic_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_batch_subtopic", columnNames = {"batch_id", "subtopic_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchSubtopicProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subtopic_id", nullable = false)
    private Subtopic subtopic;

    @Column(name = "completed_questions", nullable = false)
    @Builder.Default
    private Integer completedQuestions = 0;

    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING"; // PENDING, IN_PROGRESS, COMPLETED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;
}
