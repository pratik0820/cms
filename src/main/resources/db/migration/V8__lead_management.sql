CREATE TABLE lead_inquiries (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    is_deleted boolean NOT NULL DEFAULT false,
    lead_code varchar(40) NOT NULL UNIQUE,
    student_name varchar(255) NOT NULL,
    gender varchar(40),
    date_of_birth date,
    blood_group varchar(20),
    class_interested_in varchar(255),
    board varchar(40),
    medium varchar(80),
    stream varchar(80),
    current_school varchar(255),
    last_class_completed varchar(80),
    last_exam_percentage varchar(80),
    address varchar(2000),
    mobile_number varchar(40) NOT NULL,
    alternate_mobile_number varchar(40),
    email varchar(255),
    father_name varchar(255),
    mother_name varchar(255),
    guardian_name varchar(255),
    relation varchar(80),
    father_mobile_number varchar(40),
    mother_mobile_number varchar(40),
    guardian_mobile_number varchar(40),
    parent_email varchar(255),
    father_occupation varchar(255),
    mother_occupation varchar(255),
    annual_income varchar(80),
    nationality varchar(80),
    lead_source varchar(120) NOT NULL,
    referred_by varchar(255),
    heard_about_us varchar(255),
    preferred_branch_id uuid REFERENCES branches(id),
    course_id uuid REFERENCES courses(id),
    batch_id uuid REFERENCES batches(id),
    expected_admission_year varchar(20),
    preferred_admission_date date,
    preferred_contact_time varchar(80),
    mode_of_contact varchar(80),
    best_days_to_contact varchar(120),
    course_recommended varchar(255),
    batch_suggested varchar(255),
    admission_likelihood varchar(80),
    remarks varchar(2000),
    next_follow_up_at timestamp(6),
    status varchar(40) NOT NULL DEFAULT 'NEW',
    assigned_to_user_id uuid REFERENCES users(id),
    created_by_user_id uuid REFERENCES users(id),
    converted_student_id uuid REFERENCES students(id),
    converted_at timestamp(6)
);

CREATE TABLE lead_subjects (
    lead_id uuid NOT NULL REFERENCES lead_inquiries(id),
    subject_id uuid NOT NULL REFERENCES subjects(id),
    PRIMARY KEY (lead_id, subject_id)
);

CREATE TABLE lead_follow_ups (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    is_deleted boolean NOT NULL DEFAULT false,
    lead_id uuid NOT NULL REFERENCES lead_inquiries(id),
    follow_up_at timestamp(6) NOT NULL,
    mode_of_contact varchar(80),
    notes varchar(2000),
    next_follow_up_at timestamp(6),
    status_after varchar(40),
    created_by_user_id uuid REFERENCES users(id)
);

CREATE INDEX idx_lead_inquiries_branch ON lead_inquiries(preferred_branch_id);
CREATE INDEX idx_lead_inquiries_status ON lead_inquiries(status);
CREATE INDEX idx_lead_inquiries_source ON lead_inquiries(lead_source);
CREATE INDEX idx_lead_inquiries_follow_up ON lead_inquiries(next_follow_up_at);
CREATE INDEX idx_lead_inquiries_course ON lead_inquiries(course_id);
CREATE INDEX idx_lead_inquiries_batch ON lead_inquiries(batch_id);
CREATE INDEX idx_lead_follow_ups_lead ON lead_follow_ups(lead_id);
CREATE INDEX idx_lead_follow_ups_follow_up_at ON lead_follow_ups(follow_up_at);
