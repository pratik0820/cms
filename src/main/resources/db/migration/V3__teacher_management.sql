ALTER TABLE teachers
    ADD COLUMN IF NOT EXISTS date_of_birth date,
    ADD COLUMN IF NOT EXISTS gender varchar(30),
    ADD COLUMN IF NOT EXISTS experience_years integer,
    ADD COLUMN IF NOT EXISTS specialization varchar(255),
    ADD COLUMN IF NOT EXISTS employment_type varchar(80),
    ADD COLUMN IF NOT EXISTS salary_type varchar(80),
    ADD COLUMN IF NOT EXISTS address varchar(2000),
    ADD COLUMN IF NOT EXISTS created_by_user_id uuid REFERENCES users(id);

CREATE TABLE IF NOT EXISTS teacher_subjects (
    teacher_id uuid NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    subject varchar(120) NOT NULL,
    PRIMARY KEY (teacher_id, subject)
);

CREATE INDEX IF NOT EXISTS idx_teacher_subjects_subject ON teacher_subjects (lower(subject));
CREATE INDEX IF NOT EXISTS idx_teachers_user_id ON teachers(user_id);
CREATE INDEX IF NOT EXISTS idx_teachers_joining_date ON teachers(joining_date);
CREATE INDEX IF NOT EXISTS idx_teachers_is_active ON teachers(is_active);
