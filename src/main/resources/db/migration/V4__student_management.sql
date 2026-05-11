ALTER TABLE users
    ALTER COLUMN email DROP NOT NULL;

ALTER TABLE students
    ADD COLUMN IF NOT EXISTS student_id varchar(80),
    ADD COLUMN IF NOT EXISTS batch varchar(120),
    ADD COLUMN IF NOT EXISTS created_by_user_id uuid REFERENCES users(id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_students_student_id_active
    ON students(lower(student_id))
    WHERE student_id IS NOT NULL AND is_deleted = false;

CREATE INDEX IF NOT EXISTS idx_students_user_id ON students(user_id);
CREATE INDEX IF NOT EXISTS idx_students_standard ON students(standard);
CREATE INDEX IF NOT EXISTS idx_students_batch ON students(batch);
CREATE INDEX IF NOT EXISTS idx_students_is_active ON students(is_active);
CREATE INDEX IF NOT EXISTS idx_students_admission_date ON students(admission_date);
