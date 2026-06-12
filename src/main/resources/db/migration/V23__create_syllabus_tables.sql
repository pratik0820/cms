CREATE TABLE chapters (
    id UUID PRIMARY KEY,
    course_id UUID NOT NULL REFERENCES courses(id),
    subject_id UUID NOT NULL REFERENCES subjects(id),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_chapters_course ON chapters(course_id);
CREATE INDEX idx_chapters_subject ON chapters(subject_id);

CREATE TABLE subtopics (
    id UUID PRIMARY KEY,
    chapter_id UUID NOT NULL REFERENCES chapters(id),
    name VARCHAR(255) NOT NULL,
    no_of_questions INTEGER,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0
);
