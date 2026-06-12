-- Create timetables table for Teacher-centric workflow
CREATE TABLE timetables (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    teacher_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    effective_date DATE NOT NULL,
    view_type VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    CONSTRAINT fk_timetables_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id),
    CONSTRAINT fk_timetables_branch FOREIGN KEY (branch_id) REFERENCES branches(id)
);

CREATE INDEX idx_timetables_teacher ON timetables(teacher_id);
CREATE INDEX idx_timetables_branch ON timetables(branch_id);

-- Create timetable_entries table
CREATE TABLE timetable_entries (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    timetable_id UUID NOT NULL,
    day_of_week VARCHAR(15) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    subject_id UUID,
    batch_id UUID,
    room VARCHAR(50),
    period_type VARCHAR(20) DEFAULT 'CLASS',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    CONSTRAINT fk_tt_entry_timetable FOREIGN KEY (timetable_id) REFERENCES timetables(id),
    CONSTRAINT fk_tt_entry_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT fk_tt_entry_batch FOREIGN KEY (batch_id) REFERENCES batches(id)
);

CREATE INDEX idx_tt_entry_timetable ON timetable_entries(timetable_id);
CREATE INDEX idx_tt_entry_batch ON timetable_entries(batch_id);
CREATE INDEX idx_tt_entry_subject ON timetable_entries(subject_id);
