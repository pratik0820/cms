ALTER TABLE lead_inquiries ADD COLUMN IF NOT EXISTS total_base_fee DECIMAL(12,2);
ALTER TABLE lead_inquiries ADD COLUMN IF NOT EXISTS merit_scholarship VARCHAR(80);
ALTER TABLE lead_inquiries ADD COLUMN IF NOT EXISTS additional_concession_amount DECIMAL(12,2);
ALTER TABLE lead_inquiries ADD COLUMN IF NOT EXISTS additional_category_name VARCHAR(120);
ALTER TABLE lead_inquiries ADD COLUMN IF NOT EXISTS additional_category_discount_amount DECIMAL(12,2);
ALTER TABLE lead_inquiries ADD COLUMN IF NOT EXISTS final_payable_fee DECIMAL(12,2);
ALTER TABLE lead_inquiries ADD COLUMN IF NOT EXISTS token_amount_paid DECIMAL(12,2);
ALTER TABLE lead_inquiries ADD COLUMN IF NOT EXISTS mode_of_payment VARCHAR(80);
ALTER TABLE lead_inquiries ADD COLUMN IF NOT EXISTS payment_structure VARCHAR(80);
ALTER TABLE lead_inquiries ADD COLUMN IF NOT EXISTS financial_assistance_required BOOLEAN;
ALTER TABLE lead_inquiries ADD COLUMN IF NOT EXISTS external_scholarship_applicable BOOLEAN;
ALTER TABLE lead_inquiries ADD COLUMN IF NOT EXISTS external_scholarship_details VARCHAR(500);
ALTER TABLE lead_inquiries ADD COLUMN IF NOT EXISTS token_payment_mode VARCHAR(80);
ALTER TABLE lead_inquiries ADD COLUMN IF NOT EXISTS token_remarks VARCHAR(1000);

CREATE TABLE IF NOT EXISTS lead_inquiry_installments (
    id UUID PRIMARY KEY,
    lead_id UUID NOT NULL,
    instalment_number INT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    due_date DATE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lead_inquiry_installments_lead FOREIGN KEY (lead_id) REFERENCES lead_inquiries (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_lead_inquiry_installments_lead ON lead_inquiry_installments(lead_id);
