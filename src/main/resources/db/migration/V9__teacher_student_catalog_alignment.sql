-- Align teacher management with the canonical subject catalogue.
-- The legacy teacher_subjects text table remains for backward compatibility
-- with older payloads/screens that still submit subject labels.
CREATE TABLE IF NOT EXISTS teacher_subject_assignments (
    teacher_id UUID NOT NULL REFERENCES teachers(id),
    subject_id UUID NOT NULL REFERENCES subjects(id),
    PRIMARY KEY (teacher_id, subject_id)
);

CREATE INDEX IF NOT EXISTS idx_teacher_subject_assignments_subject
    ON teacher_subject_assignments(subject_id);

CREATE INDEX IF NOT EXISTS idx_teacher_subject_assignments_teacher
    ON teacher_subject_assignments(teacher_id);
