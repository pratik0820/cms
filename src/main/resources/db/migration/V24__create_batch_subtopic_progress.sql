CREATE TABLE batch_subtopic_progress (
    id UUID PRIMARY KEY,
    batch_id UUID NOT NULL,
    subtopic_id UUID NOT NULL,
    completed_questions INT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by_user_id UUID,
    
    CONSTRAINT fk_batch_subtopic_progress_batch FOREIGN KEY (batch_id) REFERENCES batches(id),
    CONSTRAINT fk_batch_subtopic_progress_subtopic FOREIGN KEY (subtopic_id) REFERENCES subtopics(id),
    CONSTRAINT fk_batch_subtopic_progress_user FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT uq_batch_subtopic UNIQUE (batch_id, subtopic_id)
);

CREATE INDEX idx_batch_subtopic_progress_batch ON batch_subtopic_progress(batch_id);
CREATE INDEX idx_batch_subtopic_progress_subtopic ON batch_subtopic_progress(subtopic_id);
