package com.classmanager.cms_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "timetable_entries", indexes = {
        @Index(name = "idx_tt_entry_timetable", columnList = "timetable_id"),
        @Index(name = "idx_tt_entry_batch", columnList = "batch_id"),
        @Index(name = "idx_tt_entry_subject", columnList = "subject_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_id", nullable = false)
    private Timetable timetable;

    @Column(name = "class_date")
    private LocalDate classDate;

    @Column(name = "day_of_week", length = 15)
    private String dayOfWeek; // MONDAY, TUESDAY, etc.

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @Column(name = "room", length = 50)
    private String room;

    @Column(name = "period_type", length = 20)
    @Builder.Default
    private String periodType = "CLASS"; // CLASS, BREAK, LUNCH

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
