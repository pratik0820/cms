ALTER TABLE lead_follow_ups 
ADD COLUMN spoke_with varchar(255),
ADD COLUMN remarks varchar(2000),
ADD COLUMN next_follow_up_type varchar(80),
ADD COLUMN next_follow_up_mode varchar(80),
ADD COLUMN next_follow_up_by_user_id uuid REFERENCES users(id),
ADD COLUMN reminder varchar(80),
ADD COLUMN priority varchar(40),
ADD COLUMN next_follow_up_notes varchar(2000);
