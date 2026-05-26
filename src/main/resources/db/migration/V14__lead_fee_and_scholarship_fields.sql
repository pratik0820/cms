-- D:\New\CMS\Backend\src\main\resources\db\migration\V14__lead_fee_and_scholarship_fields.sql

ALTER TABLE lead_inquiries
    ADD COLUMN total_base_fee DECIMAL(12, 2),
    ADD COLUMN payment_structure VARCHAR(40),
    ADD COLUMN mode_of_payment VARCHAR(80),
    ADD COLUMN financial_assistance_required BOOLEAN,
    ADD COLUMN external_scholarship_applicable BOOLEAN,
    ADD COLUMN external_scholarship_details VARCHAR(2000),
    ADD COLUMN previous_year_percentage VARCHAR(20),
    ADD COLUMN merit_scholarship VARCHAR(80),
    ADD COLUMN additional_category VARCHAR(255),
    ADD COLUMN additional_concession_amount DECIMAL(12, 2),
    ADD COLUMN total_scholarship_sanctioned VARCHAR(255),
    ADD COLUMN final_payable_fee DECIMAL(12, 2),
    ADD COLUMN token_amount_paid DECIMAL(12, 2),
    ADD COLUMN token_payment_mode VARCHAR(80),
    ADD COLUMN token_remarks VARCHAR(2000);
