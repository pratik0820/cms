CREATE INDEX IF NOT EXISTS idx_operational_records_branch_event_date
    ON operational_records(branch_id, event_date);

CREATE INDEX IF NOT EXISTS idx_operational_records_module_status
    ON operational_records(module, status);

CREATE INDEX IF NOT EXISTS idx_operational_records_module_type
    ON operational_records(module, type);

CREATE INDEX IF NOT EXISTS idx_students_branch_admission_date
    ON students(branch_id, admission_date);

CREATE INDEX IF NOT EXISTS idx_students_branch_standard
    ON students(branch_id, standard);

CREATE INDEX IF NOT EXISTS idx_students_branch_gender
    ON students(branch_id, gender);
