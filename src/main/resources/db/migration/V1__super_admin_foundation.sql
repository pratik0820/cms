CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE roles (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name varchar(50) NOT NULL UNIQUE,
    description varchar(255)
);

CREATE TABLE permissions (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name varchar(100) NOT NULL UNIQUE,
    description varchar(255)
);

CREATE TABLE role_permissions (
    role_id uuid NOT NULL REFERENCES roles(id),
    permission_id uuid NOT NULL REFERENCES permissions(id),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE branches (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    is_deleted boolean NOT NULL DEFAULT false,
    name varchar(255) NOT NULL,
    address varchar(255),
    city varchar(255),
    phone varchar(255),
    email varchar(255),
    is_active boolean NOT NULL DEFAULT true
);

CREATE INDEX idx_branches_name ON branches(name);

CREATE TABLE users (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    is_deleted boolean NOT NULL DEFAULT false,
    email varchar(255) NOT NULL,
    login_id varchar(255),
    password_hash varchar(255) NOT NULL,
    full_name varchar(255) NOT NULL,
    phone varchar(255),
    profile_photo_url varchar(255),
    branch_id uuid REFERENCES branches(id),
    is_active boolean NOT NULL DEFAULT true,
    last_login_at timestamp(6),
    fcm_token varchar(255),
    password_reset_token varchar(255),
    password_reset_expires_at timestamp(6),
    failed_login_attempts integer NOT NULL DEFAULT 0,
    locked_until timestamp(6)
);

CREATE UNIQUE INDEX uk_users_email_active ON users(lower(email)) WHERE is_deleted = false;
CREATE UNIQUE INDEX uk_users_login_id_active ON users(lower(login_id)) WHERE login_id IS NOT NULL AND is_deleted = false;
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_login_id ON users(login_id);

CREATE TABLE user_roles (
    user_id uuid NOT NULL REFERENCES users(id),
    role_id uuid NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE refresh_tokens (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(id),
    token_hash varchar(255) NOT NULL UNIQUE,
    expires_at timestamp(6) NOT NULL,
    used_at timestamp(6),
    revoked_at timestamp(6),
    revoked_reason varchar(255),
    ip_address varchar(255),
    user_agent varchar(255),
    created_at timestamp(6) NOT NULL
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

CREATE TABLE teachers (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    is_deleted boolean NOT NULL DEFAULT false,
    user_id uuid UNIQUE REFERENCES users(id),
    branch_id uuid NOT NULL REFERENCES branches(id),
    name varchar(255) NOT NULL,
    phone varchar(255),
    email varchar(255),
    qualification varchar(255),
    joining_date date,
    hourly_rate numeric(38,2) NOT NULL DEFAULT 0,
    is_active boolean NOT NULL DEFAULT true
);

CREATE INDEX idx_teachers_branch ON teachers(branch_id);

CREATE TABLE students (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    is_deleted boolean NOT NULL DEFAULT false,
    user_id uuid UNIQUE REFERENCES users(id),
    branch_id uuid NOT NULL REFERENCES branches(id),
    name varchar(255) NOT NULL,
    dob date,
    gender varchar(255),
    photo_url varchar(255),
    mobile varchar(255),
    parent_name varchar(255),
    parent_phone varchar(255),
    email varchar(255),
    address varchar(255),
    school_name varchar(255),
    standard varchar(255),
    board varchar(255),
    admission_date date,
    is_admission_final boolean NOT NULL DEFAULT false,
    is_active boolean NOT NULL DEFAULT true,
    CONSTRAINT students_board_check CHECK (board IN ('SSC', 'CBSE', 'ICSE') OR board IS NULL)
);

CREATE INDEX idx_students_branch ON students(branch_id);

CREATE TABLE operational_records (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    is_deleted boolean NOT NULL DEFAULT false,
    module varchar(60) NOT NULL,
    type varchar(80),
    title varchar(255) NOT NULL,
    description varchar(4000),
    status varchar(60),
    branch_id uuid REFERENCES branches(id),
    student_id uuid,
    teacher_id uuid,
    created_by_user_id uuid,
    source varchar(120),
    event_date date,
    start_time timestamp(6),
    end_time timestamp(6),
    duration_minutes integer,
    amount numeric(12,2),
    paid_amount numeric(12,2),
    pending_amount numeric(12,2),
    details_json text
);

CREATE INDEX idx_operational_records_module ON operational_records(module);
CREATE INDEX idx_operational_records_branch_module ON operational_records(branch_id, module);
CREATE INDEX idx_operational_records_event_date ON operational_records(event_date);

INSERT INTO permissions (name, description) VALUES
    ('AUTH_MANAGE', 'Manage authentication and account access'),
    ('DASHBOARD_VIEW', 'View super admin dashboard'),
    ('ADMIN_MANAGE', 'Manage admins'),
    ('TEACHER_MANAGE', 'Manage teachers'),
    ('STUDENT_MANAGE', 'Manage students'),
    ('REPORT_VIEW', 'View reports'),
    ('SETTINGS_MANAGE', 'Manage system settings');

INSERT INTO roles (name, description) VALUES
    ('SUPER_ADMIN', 'System owner'),
    ('ADMIN', 'Operational administrator'),
    ('TEACHER', 'Teacher account'),
    ('STUDENT', 'Student account'),
    ('PARENT', 'Parent account');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    'AUTH_MANAGE', 'DASHBOARD_VIEW', 'ADMIN_MANAGE', 'TEACHER_MANAGE',
    'STUDENT_MANAGE', 'REPORT_VIEW', 'SETTINGS_MANAGE'
)
WHERE r.name = 'SUPER_ADMIN';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN ('TEACHER_MANAGE', 'STUDENT_MANAGE', 'REPORT_VIEW')
WHERE r.name = 'ADMIN';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name = 'REPORT_VIEW'
WHERE r.name IN ('TEACHER', 'STUDENT', 'PARENT');
