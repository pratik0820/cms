CREATE TABLE generated_reports (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    is_deleted boolean NOT NULL DEFAULT false,
    report_name varchar(255) NOT NULL,
    category varchar(80) NOT NULL,
    report_type varchar(120) NOT NULL,
    description varchar(1000),
    format varchar(30) NOT NULL,
    status varchar(40) NOT NULL,
    branch_id uuid REFERENCES branches(id),
    from_date date,
    to_date date,
    filters_json text,
    generated_by_user_id uuid REFERENCES users(id),
    generated_on timestamp(6) NOT NULL,
    storage_provider varchar(40),
    storage_key varchar(1000),
    download_url varchar(1000),
    file_size_bytes bigint,
    expires_at timestamp(6)
);

CREATE INDEX idx_generated_reports_category ON generated_reports(category);
CREATE INDEX idx_generated_reports_generated_on ON generated_reports(generated_on);
CREATE INDEX idx_generated_reports_branch ON generated_reports(branch_id);
CREATE INDEX idx_generated_reports_type_format ON generated_reports(report_type, format);
