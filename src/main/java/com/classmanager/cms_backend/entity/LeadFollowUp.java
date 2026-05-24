package com.classmanager.cms_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lead_follow_ups", indexes = {
        @Index(name = "idx_lead_follow_ups_lead", columnList = "lead_id"),
        @Index(name = "idx_lead_follow_ups_follow_up_at", columnList = "follow_up_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadFollowUp extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    private LeadInquiry lead;

    @Column(name = "follow_up_at", nullable = false)
    private LocalDateTime followUpAt;

    @Column(name = "mode_of_contact", length = 80)
    private String modeOfContact;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "next_follow_up_at")
    private LocalDateTime nextFollowUpAt;

    @Column(name = "status_after", length = 40)
    private String statusAfter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @Column(name = "spoke_with")
    private String spokeWith;

    @Column(name = "remarks", length = 2000)
    private String remarks;

    @Column(name = "next_follow_up_type", length = 80)
    private String nextFollowUpType;

    @Column(name = "next_follow_up_mode", length = 80)
    private String nextFollowUpMode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_follow_up_by_user_id")
    private User nextFollowUpByUser;

    @Column(name = "reminder", length = 80)
    private String reminder;

    @Column(name = "priority", length = 40)
    private String priority;

    @Column(name = "next_follow_up_notes", length = 2000)
    private String nextFollowUpNotes;
}
