CREATE TABLE IF NOT EXISTS teacher_course_assignments (
    teacher_id UUID NOT NULL REFERENCES teachers(id),
    course_id UUID NOT NULL REFERENCES courses(id),
    PRIMARY KEY (teacher_id, course_id)
);

CREATE INDEX IF NOT EXISTS idx_teacher_course_assignments_teacher
    ON teacher_course_assignments(teacher_id);

CREATE INDEX IF NOT EXISTS idx_teacher_course_assignments_course
    ON teacher_course_assignments(course_id);

CREATE TABLE IF NOT EXISTS teacher_batch_assignments (
    teacher_id UUID NOT NULL REFERENCES teachers(id),
    batch_id UUID NOT NULL REFERENCES batches(id),
    PRIMARY KEY (teacher_id, batch_id)
);

CREATE INDEX IF NOT EXISTS idx_teacher_batch_assignments_teacher
    ON teacher_batch_assignments(teacher_id);

CREATE INDEX IF NOT EXISTS idx_teacher_batch_assignments_batch
    ON teacher_batch_assignments(batch_id);
