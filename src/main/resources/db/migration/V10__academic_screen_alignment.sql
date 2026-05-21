-- ============================================================
-- V10: Academic screen alignment for standards, courses, and subjects
-- ============================================================

CREATE TABLE standards (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at  timestamp(6) NOT NULL,
    updated_at  timestamp(6) NOT NULL,
    is_deleted  boolean NOT NULL DEFAULT false,
    code        varchar(20) NOT NULL UNIQUE,
    name        varchar(30) NOT NULL,
    board       varchar(20) NOT NULL,
    is_active   boolean NOT NULL DEFAULT true,
    sort_order  integer NOT NULL DEFAULT 0
);

CREATE INDEX idx_standards_code ON standards(code);
CREATE INDEX idx_standards_name_board ON standards(name, board);

INSERT INTO standards (id, created_at, updated_at, code, name, board, is_active, sort_order)
SELECT gen_random_uuid(),
       now(),
       now(),
       'STD' || lpad(row_number() over (order by board_name, standard_name)::text, 4, '0'),
       standard_name,
       board_name,
       true,
       row_number() over (order by board_name, standard_name)
FROM (
    SELECT DISTINCT
        trim(standard) AS standard_name,
        CASE
            WHEN board IS NULL AND standard IN ('11', '12', '11-12') THEN 'HSC'
            WHEN board IS NULL THEN 'SSC'
            ELSE board
        END AS board_name
    FROM courses
    WHERE is_deleted = false
) standard_seed;

ALTER TABLE courses ADD COLUMN IF NOT EXISTS standard_id uuid REFERENCES standards(id);
ALTER TABLE courses ADD COLUMN IF NOT EXISTS medium varchar(50);

UPDATE courses c
SET standard_id = s.id
FROM standards s
WHERE s.name = c.standard
  AND s.board = CASE
        WHEN c.board IS NULL AND c.standard IN ('11', '12', '11-12') THEN 'HSC'
        WHEN c.board IS NULL THEN 'SSC'
        ELSE c.board
      END
  AND c.standard_id IS NULL;

ALTER TABLE subjects ADD COLUMN IF NOT EXISTS display_code varchar(20);
ALTER TABLE batches ADD COLUMN IF NOT EXISTS display_code varchar(20);

UPDATE subjects
SET display_code = generated.display_code
FROM (
    SELECT id, 'SUB' || lpad(row_number() over (order by sort_order, display_name)::text, 4, '0') AS display_code
    FROM subjects
    WHERE display_code IS NULL
) generated
WHERE subjects.id = generated.id;

UPDATE batches
SET display_code = generated.display_code
FROM (
    SELECT id, 'CRS' || lpad(row_number() over (order by created_at, name)::text, 4, '0') AS display_code
    FROM batches
    WHERE display_code IS NULL
) generated
WHERE batches.id = generated.id;

ALTER TABLE subjects ADD CONSTRAINT uq_subjects_display_code UNIQUE (display_code);
ALTER TABLE batches ADD CONSTRAINT uq_batches_display_code UNIQUE (display_code);

CREATE TABLE course_subjects (
    course_id    uuid NOT NULL REFERENCES courses(id),
    subject_id   uuid NOT NULL REFERENCES subjects(id),
    PRIMARY KEY (course_id, subject_id)
);

INSERT INTO course_subjects (course_id, subject_id)
SELECT DISTINCT sg.course_id, sgs.subject_id
FROM subject_groups sg
JOIN subject_group_subjects sgs ON sgs.subject_group_id = sg.id
LEFT JOIN course_subjects cs ON cs.course_id = sg.course_id AND cs.subject_id = sgs.subject_id
WHERE cs.course_id IS NULL;
