-- V12: Add missing fields to lead_inquiries table
-- Fields identified from the Lead Inquiry form that were absent from the schema

ALTER TABLE lead_inquiries
    -- Contact: Country codes for all phone fields
    ADD COLUMN IF NOT EXISTS mobile_country_code           varchar(10),
    ADD COLUMN IF NOT EXISTS alternate_mobile_country_code varchar(10),
    ADD COLUMN IF NOT EXISTS father_mobile_country_code    varchar(10),
    ADD COLUMN IF NOT EXISTS mother_mobile_country_code    varchar(10),
    ADD COLUMN IF NOT EXISTS guardian_mobile_country_code  varchar(10),

    -- Lead Source: Inquiry type (New Admission / Transfer / Other)
    ADD COLUMN IF NOT EXISTS inquiry_for                   varchar(80),

    -- Counsellor's Recommendation: subjects suggested (free-text; structured via lead_recommended_subjects)
    ADD COLUMN IF NOT EXISTS subjects_suggested             varchar(2000),

    -- Counsellor assignment (can overlap with assigned_to_user_id but explicit counsellor is useful)
    ADD COLUMN IF NOT EXISTS counsellor_user_id            uuid REFERENCES users(id);

-- Join table: subjects recommended by counsellor (structured, reuses subjects catalogue)
CREATE TABLE IF NOT EXISTS lead_recommended_subjects (
    lead_id    uuid NOT NULL REFERENCES lead_inquiries(id) ON DELETE CASCADE,
    subject_id uuid NOT NULL REFERENCES subjects(id),
    PRIMARY KEY (lead_id, subject_id)
);

CREATE INDEX IF NOT EXISTS idx_lead_recommended_subjects_lead ON lead_recommended_subjects(lead_id);
CREATE INDEX IF NOT EXISTS idx_lead_inquiries_counsellor       ON lead_inquiries(counsellor_user_id);
CREATE INDEX IF NOT EXISTS idx_lead_inquiries_inquiry_for      ON lead_inquiries(inquiry_for);
