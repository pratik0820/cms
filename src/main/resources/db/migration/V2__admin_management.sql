CREATE TABLE admin_profiles (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    is_deleted boolean NOT NULL DEFAULT false,
    user_id uuid NOT NULL UNIQUE REFERENCES users(id),
    date_of_birth date,
    gender varchar(30),
    role_title varchar(80) NOT NULL,
    joining_date date NOT NULL,
    access_level varchar(80) NOT NULL,
    address varchar(2000),
    all_branches_access boolean NOT NULL DEFAULT true,
    created_by_user_id uuid REFERENCES users(id)
);

CREATE INDEX idx_admin_profiles_user_id ON admin_profiles(user_id);
CREATE INDEX idx_admin_profiles_joining_date ON admin_profiles(joining_date);
CREATE INDEX idx_admin_profiles_all_branches_access ON admin_profiles(all_branches_access);
