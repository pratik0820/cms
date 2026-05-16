-- ============================================================
-- V7: Courses, Batches, Subject Groups and Student Enrolments
-- ============================================================
-- This migration creates the academic catalogue and enrolment system:
--   subjects          → master subject list
--   courses           → academic offerings (8th CBSE, 11-12 HSC+JEE, etc.)
--   subject_groups    → selectable subject bundles per course
--   batches           → running instances of a course at a branch
--   student_enrolments → student enrolments with manually entered fees
--
-- Design note:
--   Fee amounts are entered manually by the admin at enrolment time.
--   No backend calculation is performed. No fee structure reference data.
-- ============================================================

-- ── SUBJECTS ────────────────────────────────────────────────
CREATE TABLE subjects (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at      timestamp(6) NOT NULL,
    updated_at      timestamp(6) NOT NULL,
    is_deleted      boolean NOT NULL DEFAULT false,
    code            varchar(40)  NOT NULL UNIQUE,
    display_name    varchar(100) NOT NULL,
    short_name      varchar(20),
    description     varchar(500),
    sort_order      integer NOT NULL DEFAULT 0,
    is_active       boolean NOT NULL DEFAULT true
);
CREATE INDEX idx_subjects_code ON subjects(code);

-- ── COURSES ─────────────────────────────────────────────────
CREATE TABLE courses (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at      timestamp(6) NOT NULL,
    updated_at      timestamp(6) NOT NULL,
    is_deleted      boolean NOT NULL DEFAULT false,
    name            varchar(150) NOT NULL,
    code            varchar(40)  NOT NULL UNIQUE,
    category        varchar(30)  NOT NULL,
    board           varchar(10),
    standard        varchar(10)  NOT NULL,
    academic_year   varchar(10),
    description     varchar(500),
    is_active       boolean NOT NULL DEFAULT true,
    sort_order      integer NOT NULL DEFAULT 0
);
CREATE INDEX idx_courses_board_standard ON courses(board, standard);
CREATE INDEX idx_courses_category       ON courses(category);

-- ── SUBJECT GROUPS ──────────────────────────────────────────
CREATE TABLE subject_groups (
    id                      uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at              timestamp(6) NOT NULL,
    updated_at              timestamp(6) NOT NULL,
    is_deleted              boolean NOT NULL DEFAULT false,
    course_id               uuid NOT NULL REFERENCES courses(id),
    name                    varchar(150) NOT NULL,
    short_name              varchar(40),
    subject_count           integer NOT NULL,
    is_extra_subject_allowed boolean NOT NULL DEFAULT false,
    max_extra_subjects      integer NOT NULL DEFAULT 0,
    sort_order              integer NOT NULL DEFAULT 0,
    is_active               boolean NOT NULL DEFAULT true
);
CREATE INDEX idx_subject_groups_course ON subject_groups(course_id);

-- subjects included in a group (default set)
CREATE TABLE subject_group_subjects (
    subject_group_id uuid NOT NULL REFERENCES subject_groups(id),
    subject_id       uuid NOT NULL REFERENCES subjects(id),
    PRIMARY KEY (subject_group_id, subject_id)
);

-- subjects that can be added as extras to a group
CREATE TABLE subject_group_extra_subjects (
    subject_group_id uuid NOT NULL REFERENCES subject_groups(id),
    subject_id       uuid NOT NULL REFERENCES subjects(id),
    PRIMARY KEY (subject_group_id, subject_id)
);

-- ── BATCHES ─────────────────────────────────────────────────
CREATE TABLE batches (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at          timestamp(6) NOT NULL,
    updated_at          timestamp(6) NOT NULL,
    is_deleted          boolean NOT NULL DEFAULT false,
    branch_id           uuid NOT NULL REFERENCES branches(id),
    course_id           uuid NOT NULL REFERENCES courses(id),
    name                varchar(200) NOT NULL,
    academic_year       varchar(10)  NOT NULL,
    batch_type          varchar(20),
    timing              varchar(10)  NOT NULL,
    timing_label        varchar(60),
    start_time          time,
    end_time            time,
    days_of_week        varchar(60),
    start_date          date,
    end_date            date,
    max_students        integer NOT NULL DEFAULT 0,
    enrolled_count      integer NOT NULL DEFAULT 0,
    class_teacher_id    uuid REFERENCES teachers(id),
    room                varchar(50),
    is_active           boolean NOT NULL DEFAULT true
);
CREATE INDEX idx_batches_branch        ON batches(branch_id);
CREATE INDEX idx_batches_course        ON batches(course_id);
CREATE INDEX idx_batches_academic_year ON batches(academic_year);

-- ── STUDENT ENROLMENTS ──────────────────────────────────────
CREATE TABLE student_enrolments (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at       timestamp(6) NOT NULL,
    updated_at       timestamp(6) NOT NULL,
    is_deleted       boolean NOT NULL DEFAULT false,
    student_id       uuid NOT NULL REFERENCES students(id),
    batch_id         uuid NOT NULL REFERENCES batches(id),
    academic_year    varchar(10)  NOT NULL,
    subject_group_id uuid REFERENCES subject_groups(id),
    agreed_total_fee numeric(12,2) NOT NULL,
    payment_plan     varchar(20)  NOT NULL,
    enrolment_date   date NOT NULL,
    status           varchar(20)  NOT NULL DEFAULT 'ACTIVE',
    notes            varchar(1000)
);

CREATE INDEX idx_enrolments_student      ON student_enrolments(student_id);
CREATE INDEX idx_enrolments_batch        ON student_enrolments(batch_id);
CREATE INDEX idx_enrolments_academic_year ON student_enrolments(academic_year);
CREATE INDEX idx_enrolments_status       ON student_enrolments(status);

-- Subjects the student is enrolled for (actual selection)
CREATE TABLE enrolment_subjects (
    enrolment_id uuid NOT NULL REFERENCES student_enrolments(id),
    subject_id   uuid NOT NULL REFERENCES subjects(id),
    PRIMARY KEY (enrolment_id, subject_id)
);

-- Admin-entered instalment schedule
CREATE TABLE enrolment_instalments (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at          timestamp(6) NOT NULL,
    updated_at          timestamp(6) NOT NULL,
    is_deleted          boolean NOT NULL DEFAULT false,
    enrolment_id        uuid NOT NULL REFERENCES student_enrolments(id),
    instalment_number   integer NOT NULL,
    label               varchar(200) NOT NULL,
    amount              numeric(12,2) NOT NULL,
    due_date            date,
    is_post_dated_cheque boolean NOT NULL DEFAULT false,
    is_paid             boolean NOT NULL DEFAULT false,
    paid_date           date
);

CREATE INDEX idx_enrolment_instalments_enrolment ON enrolment_instalments(enrolment_id);
CREATE INDEX idx_enrolment_instalments_due_date  ON enrolment_instalments(due_date);

-- ============================================================
-- SEED: Master Subjects
-- ============================================================
INSERT INTO subjects (id, created_at, updated_at, code, display_name, short_name, sort_order) VALUES
    (gen_random_uuid(), now(), now(), 'MATHS',          'Mathematics',              'Maths',    1),
    (gen_random_uuid(), now(), now(), 'SCIENCE',        'Science',                  'Science',  2),
    (gen_random_uuid(), now(), now(), 'ENGLISH',        'English',                  'English',  3),
    (gen_random_uuid(), now(), now(), 'LANGUAGE',       'Language (Marathi/Hindi)', 'Language', 4),
    (gen_random_uuid(), now(), now(), 'SST',            'Social Studies',           'SST',      5),
    (gen_random_uuid(), now(), now(), 'HINDI',          'Hindi',                    'Hindi',    6),
    (gen_random_uuid(), now(), now(), 'MARATHI',        'Marathi',                  'Marathi',  7),
    (gen_random_uuid(), now(), now(), 'SANSKRIT',       'Sanskrit',                 'Sanskrit', 8),
    (gen_random_uuid(), now(), now(), 'GERMAN',         'German',                   'German',   9),
    (gen_random_uuid(), now(), now(), 'HISTORY',        'History',                  'History',  10),
    (gen_random_uuid(), now(), now(), 'GEOGRAPHY',      'Geography',                'Geo',      11),
    (gen_random_uuid(), now(), now(), 'CIVICS',         'Civics',                   'Civics',   12),
    (gen_random_uuid(), now(), now(), 'PHYSICS',        'Physics',                  'Physics',  13),
    (gen_random_uuid(), now(), now(), 'CHEMISTRY',      'Chemistry',                'Chem',     14),
    (gen_random_uuid(), now(), now(), 'BIOLOGY',        'Biology',                  'Bio',      15),
    (gen_random_uuid(), now(), now(), 'MATHS_ADVANCED', 'Mathematics (Advanced)',   'Maths+',   16),
    (gen_random_uuid(), now(), now(), 'ECONOMICS',      'Economics',                'Eco',      17),
    (gen_random_uuid(), now(), now(), 'ACCOUNTS',       'Accounts',                 'Acc',      18),
    (gen_random_uuid(), now(), now(), 'BUSINESS_STUDIES','Business Studies',       'BSt',      19),
    (gen_random_uuid(), now(), now(), 'LITERATURE',     'Literature',               'Lit',      20),
    (gen_random_uuid(), now(), now(), 'COMP_APP',       'Computer Applications',    'CompApp',  21),
    (gen_random_uuid(), now(), now(), 'OTHER',          'Other',                    'Other',    99);

-- ============================================================
-- SEED: Sample Courses (can be customized by admin)
-- ============================================================
-- SSC Board: 8th, 9th, 10th
INSERT INTO courses (id, created_at, updated_at, code, name, category, board, standard, sort_order) VALUES
    ('a0000001-0000-0000-0000-000000000001', now(), now(), 'SSC-8',    'Std. 8th SSC Batch',    'BOARD_REGULAR', 'SSC',  '8',    10),
    ('a0000001-0000-0000-0000-000000000002', now(), now(), 'SSC-9',    'Std. 9th SSC Batch',    'BOARD_REGULAR', 'SSC',  '9',    20),
    ('a0000001-0000-0000-0000-000000000003', now(), now(), 'SSC-10',   'Std. 10th SSC Batch',   'BOARD_REGULAR', 'SSC',  '10',   30),
-- CBSE Board: 8th, 9th, 10th
    ('a0000001-0000-0000-0000-000000000004', now(), now(), 'CBSE-8',   'Std. 8th CBSE Batch',   'BOARD_REGULAR', 'CBSE', '8',    40),
    ('a0000001-0000-0000-0000-000000000005', now(), now(), 'CBSE-9',   'Std. 9th CBSE Batch',   'BOARD_REGULAR', 'CBSE', '9',    50),
    ('a0000001-0000-0000-0000-000000000006', now(), now(), 'CBSE-10',  'Std. 10th CBSE Batch',  'BOARD_REGULAR', 'CBSE', '10',   60),
-- ICSE Board: 8th, 9th, 10th
    ('a0000001-0000-0000-0000-000000000007', now(), now(), 'ICSE-8',   'Std. 8th ICSE Batch',   'BOARD_REGULAR', 'ICSE', '8',    70),
    ('a0000001-0000-0000-0000-000000000008', now(), now(), 'ICSE-9',   'Std. 9th ICSE Batch',   'BOARD_REGULAR', 'ICSE', '9',    80),
    ('a0000001-0000-0000-0000-000000000009', now(), now(), 'ICSE-10',  'Std. 10th ICSE Batch',  'BOARD_REGULAR', 'ICSE', '10',   90),
-- HSC 11th-12th
    ('a0000001-0000-0000-0000-000000000010', now(), now(), 'HSC-11-12','Std. 11th-12th HSC',    'BOARD_SENIOR',  NULL,   '11-12',100);

-- ============================================================
-- SEED: Sample Subject Groups (can be customized by admin)
-- ============================================================
-- SSC 8th: Chanakya (5 subjects), Drona (4), Vyasa (3), Arjuna (2)
INSERT INTO subject_groups (id, created_at, updated_at, course_id, name, short_name, subject_count, is_extra_subject_allowed, max_extra_subjects, sort_order) VALUES
    ('b0000001-0000-0000-0000-000000000001', now(), now(), 'a0000001-0000-0000-0000-000000000001', 'Chanakya - All Subjects', 'Chanakya', 5, false, 0, 1),
    ('b0000001-0000-0000-0000-000000000002', now(), now(), 'a0000001-0000-0000-0000-000000000001', 'Drona',                   'Drona',    4, false, 0, 2),
    ('b0000001-0000-0000-0000-000000000003', now(), now(), 'a0000001-0000-0000-0000-000000000001', 'Vyasa',                   'Vyasa',    3, true,  2, 3),
    ('b0000001-0000-0000-0000-000000000004', now(), now(), 'a0000001-0000-0000-0000-000000000001', 'Arjuna',                  'Arjuna',   2, true,  3, 4);

-- SSC 8th: subject_group_subjects
INSERT INTO subject_group_subjects (subject_group_id, subject_id)
SELECT 'b0000001-0000-0000-0000-000000000001', id FROM subjects WHERE code IN ('MATHS','SCIENCE','ENGLISH','LANGUAGE','SST');
INSERT INTO subject_group_subjects (subject_group_id, subject_id)
SELECT 'b0000001-0000-0000-0000-000000000002', id FROM subjects WHERE code IN ('MATHS','SCIENCE','ENGLISH','SST');
INSERT INTO subject_group_subjects (subject_group_id, subject_id)
SELECT 'b0000001-0000-0000-0000-000000000003', id FROM subjects WHERE code IN ('MATHS','SCIENCE','ENGLISH');
INSERT INTO subject_group_subjects (subject_group_id, subject_id)
SELECT 'b0000001-0000-0000-0000-000000000004', id FROM subjects WHERE code IN ('MATHS','SCIENCE');

-- SSC 8th: allowed extras for Vyasa and Arjuna
INSERT INTO subject_group_extra_subjects (subject_group_id, subject_id)
SELECT 'b0000001-0000-0000-0000-000000000003', id FROM subjects WHERE code IN ('LANGUAGE','SST');
INSERT INTO subject_group_extra_subjects (subject_group_id, subject_id)
SELECT 'b0000001-0000-0000-0000-000000000004', id FROM subjects WHERE code IN ('ENGLISH','LANGUAGE','SST');
