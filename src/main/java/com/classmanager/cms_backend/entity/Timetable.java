package com.classmanager.cms_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "timetables", indexes = {
        @Index(name = "idx_timetables_teacher", columnList = "teacher_id"),
        @Index(name = "idx_timetables_branch", columnList = "branch_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timetable extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "view_type", length = 20)
    private String viewType;

    @OneToMany(mappedBy = "timetable", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TimetableEntry> entries = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public void addEntry(TimetableEntry entry) {
        entries.add(entry);
        entry.setTimetable(this);
    }

    public void removeEntry(TimetableEntry entry) {
        entries.remove(entry);
        entry.setTimetable(null);
    }
}
