-- 2025-09-14_add_department_id_to_app_user.sql
ALTER TABLE app_user
ADD COLUMN department_id UUID REFERENCES department(id);

ALTER TABLE app_user
ADD COLUMN email varchar(255) NOT NULL;