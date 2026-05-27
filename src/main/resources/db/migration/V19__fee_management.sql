ALTER TABLE enrolment_instalments ADD COLUMN IF NOT EXISTS paid_amount NUMERIC(12, 2) DEFAULT 0.00 NOT NULL;

CREATE TABLE IF NOT EXISTS fee_transactions (
    id UUID PRIMARY KEY,
    enrolment_id UUID NOT NULL REFERENCES student_enrolments(id),
    transaction_date TIMESTAMP NOT NULL,
    payment_mode VARCHAR(50) NOT NULL,
    reference_no VARCHAR(255),
    remarks TEXT,
    total_selected_amount NUMERIC(12, 2) NOT NULL,
    discount_amount NUMERIC(12, 2) DEFAULT 0.00,
    late_fee_amount NUMERIC(12, 2) DEFAULT 0.00,
    total_payable_amount NUMERIC(12, 2) NOT NULL,
    amount_received NUMERIC(12, 2) NOT NULL,
    receipt_url VARCHAR(1000),
    created_by_user_id UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_fee_transactions_enrolment ON fee_transactions(enrolment_id);

CREATE TABLE IF NOT EXISTS fee_transaction_details (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL REFERENCES fee_transactions(id),
    instalment_id UUID NOT NULL REFERENCES enrolment_instalments(id),
    allocated_amount NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_fee_transaction_details_tx ON fee_transaction_details(transaction_id);
